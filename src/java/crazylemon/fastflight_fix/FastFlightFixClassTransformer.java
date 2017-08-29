package crazylemon.fastflight_fix;

import static org.objectweb.asm.Opcodes.GOTO;

import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class FastFlightFixClassTransformer implements IClassTransformer {

	private static final String[] classesToTransform = {
		"net.minecraft.network.NetHandlerPlayServer"
	};
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		int index = Arrays.asList(classesToTransform).indexOf(transformedName);
		boolean isObfuscated = !name.equals(transformedName);
		return index == -1 ? basicClass : transform(index, basicClass, isObfuscated);
	}
	
	private static byte[] transform(int index, byte[] basicClass, boolean isObfuscated) {
		try
		{
			ClassNode cn = new ClassNode();
			ClassReader cr = new ClassReader(basicClass);
			cr.accept(cn, 0);
			
			switch(index) {
			case 0:
				transformNetHandlerPlayServer(cn, isObfuscated);
				break;
			}
			
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			
			cn.accept(cw);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return basicClass;
	}
	
	private static void transformNetHandlerPlayServer(ClassNode netHandlerPlayServerClass, boolean isObfuscated) {
		final String PROCESS_PLAYER = isObfuscated ? "a" : "processPlayer";
		final String PROCESS_PLAYER_DESC = isObfuscated ? "(Liw;)V" : "(Lnet/minecraft/network/play/client/CPacketPlayer;)V";
		
		for(MethodNode method : netHandlerPlayServerClass.methods) {
			if(method.name.equals(PROCESS_PLAYER) && method.desc.equals(PROCESS_PLAYER_DESC)) {
				AbstractInsnNode startNode = null, endNode = null;
				for(AbstractInsnNode instruction : method.instructions.toArray()) {
					if(instruction.getType() == AbstractInsnNode.LINE) {
						switch(((LineNumberNode) instruction).line) {
						case 477:
							startNode = instruction.getNext();
							break;
						case 489:
							endNode = instruction.getPrevious();
							break;
						default:
							break;
						}
					}
					if(startNode != null && endNode != null) {
						// We found what we were looking for
						break;
					}
				}
				// We have our nodes, now to skip this section
				LabelNode end = new LabelNode();
				InsnList toInsert = new InsnList();
				toInsert.add(new JumpInsnNode(GOTO, end));
				
				method.instructions.insertBefore(startNode, toInsert);
				method.instructions.insert(endNode, end);
			}
		}
	}
}

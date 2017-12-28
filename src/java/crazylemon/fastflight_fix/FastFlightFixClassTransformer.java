package crazylemon.fastflight_fix;

import static org.objectweb.asm.Opcodes.GOTO;

import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

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
		return cw.toByteArray();
	}
	
	private static void transformNetHandlerPlayServer(ClassNode netHandlerPlayServerClass, boolean isObfuscated) {
		final String PROCESS_PLAYER = isObfuscated ? "a" : "processPlayer";
		final String PROCESS_PLAYER_DESC = isObfuscated ? "(Llk;)V" : "(Lnet/minecraft/network/play/client/CPacketPlayer;)V";
		FastFlightFixModContainer.logInfo("Modifying class '%s'", netHandlerPlayServerClass.name);
		
		for(MethodNode method : netHandlerPlayServerClass.methods) {
			if(method.name.equals(PROCESS_PLAYER) && method.desc.equals(PROCESS_PLAYER_DESC)) {
				FastFlightFixModContainer.logInfo("	Modifying method '%s'", method.name);
				AbstractInsnNode startNode = null, endNode = null;
				for(AbstractInsnNode instruction : method.instructions.toArray()) {
					if(instruction.getType() != AbstractInsnNode.LDC_INSN) {
						continue; // Not our mark
					}
					if(!(((LdcInsnNode)instruction).cst instanceof String)) {
						continue;
					}
					if(!(((LdcInsnNode)instruction).cst).equals("{} moved too quickly! {},{},{}")) {
						continue;
					}
					// We have a reference point, now to march a set distance in both directions
					// and plant down our hooks
					AbstractInsnNode n = instruction;
					for(int i = 0; i < 54; i++, n = n.getPrevious());
					// This should be the ALOAD right after line 543
					startNode = n;
					// Reset our position back to the middle
					n = instruction;
					for(int i = 0; i < 46; i++, n = n.getNext());
					// This should be the label before line 556
					endNode = n;
				}
				
				// Make sure what we think we're looking, is
				if(startNode == null || endNode == null) {
					FastFlightFixModContainer.die("Didn't find the line end points!");
				}
				if(!(startNode instanceof VarInsnNode)) {
					FastFlightFixModContainer.die("startNode was not a VarInsnNode");
				}
				if(!(endNode instanceof LabelNode)) {
					FastFlightFixModContainer.die("endNode was not a LabelNode");
				}

				method.instructions.insertBefore(startNode, new JumpInsnNode(GOTO, (LabelNode)endNode));
				
				FastFlightFixModContainer.logInfo("	Modification of method '%s' succeeded", method.name);
			}
		}
	}
}

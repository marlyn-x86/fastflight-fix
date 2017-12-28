package crazylemon.fastflight_fix;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Arrays;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.InsnNode;
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
		if(index != -1)
		{
			boolean isObfuscated = !name.equals(transformedName);
			FastFlightFixModContainer.logInfo("Original Class: '%s'", transformedName);
			FastFlightFixModContainer.logInfo("Obfuscated Class: '%s'", name);
			
			return transform(index, basicClass, isObfuscated);
		}
		return basicClass;
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
		final String METHOD_INVDIM = isObfuscated ? "L" : "isInvulnerableDimensionChange";
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
					final int HOPBACK_OFFSET = 58;
					final int HOPFWD_OFFSET = 33;
					for(int i = 0; i < HOPBACK_OFFSET; i++, n = n.getPrevious());
					// This should be the ALOAD right after line 543
					startNode = n;
					// Reset our position back to the middle
					n = instruction;
					for(int i = 0; i < HOPFWD_OFFSET; i++, n = n.getNext());
					// This should be the label before line 555
					endNode = n;
					
					n = startNode;
					for(int i = 0; i < HOPBACK_OFFSET + HOPFWD_OFFSET + 1; i++, n = n.getNext()) {
						FastFlightFixModContainer.logInfo("	 %d: '%s'", i, n.toString());
					}
					break;
				}

				// Make sure what we think we're looking, is
				if(startNode == null || endNode == null) {
					FastFlightFixModContainer.die("Didn't find the line end points!");
				}
				if(!(startNode instanceof VarInsnNode)) {
					FastFlightFixModContainer.die("startNode was not a VarInsnNode");
				}
				else
				{
					// startNode is sanity-checked, now to check the neighbors
					AbstractInsnNode n = startNode;
					boolean weHadRightContext = false;
					// hop fwd 2
					for(int i = 0; i < 2; i++, n = n.getNext());
					if(n instanceof MethodInsnNode)
					{
						MethodInsnNode mn = (MethodInsnNode)n;
						if(mn.name.equals(METHOD_INVDIM))
						{
							weHadRightContext = true;
						}
						else
						{
							FastFlightFixModContainer.logInfo("	Unexpected method found: '%s'", mn.name);
						}
					}
					
					if(!weHadRightContext)
					{
						FastFlightFixModContainer.die("startNode did not have the right context!");
					}
				}
				if(!(endNode instanceof LabelNode)) {
					FastFlightFixModContainer.die("endNode was not a LabelNode");
				}
				else
				{
					// endNode is sanity-checked, now to check the neighbors
					AbstractInsnNode n = endNode;
					boolean weHadRightContext = false;
					// hop back 1
					for(int i = 0; i < 1; i++, n = n.getPrevious());
					if(n instanceof InsnNode)
					{
						InsnNode in = (InsnNode)n;
						if(n.getOpcode() == RETURN)
						{
							weHadRightContext = true;
						}
					}
					
					if(!weHadRightContext)
					{
						FastFlightFixModContainer.die("endNode did not come immediately after a return");
					}
				}

				method.instructions.insertBefore(startNode, new JumpInsnNode(GOTO, (LabelNode)endNode));
				
				FastFlightFixModContainer.logInfo("	Modification of method '%s' succeeded", method.name);
			}
		}
	}
}

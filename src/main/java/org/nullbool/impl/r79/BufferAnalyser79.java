package org.nullbool.impl.r79;

import java.util.List;

import org.nullbool.api.Builder;
import org.nullbool.api.analysis.IMethodAnalyser;
import org.nullbool.api.analysis.SupportedHooks;
import org.nullbool.api.obfuscation.cfg.ControlFlowGraph;
import org.nullbool.impl.r77.BufferAnalyser77;
import org.objectweb.asm.tree.MethodNode;
import org.topdank.banalysis.filter.Filter;
import org.zbot.hooks.MethodHook;
import org.zbot.hooks.MethodHook.MethodType;

@SupportedHooks(fields  = { "getPayload&[B", "getCaret&I", }, 
methods = { "enableEncryption&(Ljava/math/BigInteger;Ljava/math/BigInteger;)V",
		"writeVarByte&(I)V", "writeBytes&([BIII)V",
		"write8&(I)V", "write8Weird&(I)V", "write16&(I)V", "write16A&(I)V", "write16B&(I)V", "write24&(I)V", "write32&(I)V", "write40&(J)V", "write64&(J)V",
		"writeLE16&(I)V", "writeLE16A&(I)V", "writeLE32&(I)V", "write32Weird&(I)V",
		"writeInverted32&(I)V", /*"writeInverted24&(I)V",*/ "writeInvertedLE32&(I)V",
		"writeString&(Ljava/lang/String;)V", "writeJagexString&(Ljava/lang/String;)V", "writeCharSequence&(Ljava/lang/CharSequence;)V",
		
		"readBytesA&([BII)V", "readBytesB&([BII)V"
})

/**
 * "readBytesA&([BII)V", "readBytesB&([BII)V" are only for rev 79+
 * 
 * @author Bibl (don't ban me pls)
 * @created 7 Jun 2015 14:00:17
 */
public class BufferAnalyser79 extends BufferAnalyser77 {

	/**
	 * 
	 */
	public BufferAnalyser79() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Builder<IMethodAnalyser> registerMethodAnalysers() {
		return super.registerMethodAnalysers().replace(new Filter<IMethodAnalyser>() {
			
			@Override
			public boolean accept(IMethodAnalyser t) {
				return t.getClass().equals(MethodAnalyser77.class);
			}
		}, new MethodAnalyser79());
	}
	
	/**
	 * @author Bibl (don't ban me pls)
	 * @created 7 Jun 2015 10:44:24
	 */
	public class MethodAnalyser79 extends MethodAnalyser77 {
		
		@Override
		protected void analyse(ArrayStoreVisitor asv, ControlFlowGraph graph, MethodNode m, List<MethodHook> list) {
			boolean b = false;
			if(m.desc.startsWith("(J") && m.desc.endsWith("V")) {
				List<Object> value = asv.found.get(ArrayStoreVisitor.VALUE);
				if(match(value, WRITE_LONG_SHIFTS, 2)) {
					// NOTE: ADDED IN R77
					list.add(asMethodHook(MethodType.CALLBACK, m, "write40"));
					b = true;
				}
			} 

			if(!b) {
				super.analyse(asv, graph, m, list);
			}
		}
		
		@Override
		public void analyseMultiByte(ArrayStoreVisitor asv, ArrayMethodVisitor amv, ControlFlowGraph graph, MethodNode m, List<MethodHook> list) {
			boolean b = false;
			
			List<Object> value = asv.found.get(ArrayStoreVisitor.VALUE);
			if(WRITE_BYTES2.equals(amv.set) && match(value, WRITE_BYTES_SUB)) {
				if(amv.opcode == IF_ICMPLT) {
					list.add(asMethodHook(MethodType.CALLBACK, m, "readBytesB"));
					b = true;
				} else if(amv.opcode == IF_ICMPGE) {
					list.add(asMethodHook(MethodType.CALLBACK, m, "readBytesA"));
					b = true;
				} else {
					throw new RuntimeException();
				}
			}
			
			//if(WRITE_BYTES2.equals(amv.set)) {
			//	System.out.println("for " + m + " is " + amv.set + " " + Printer.OPCODES[amv.opcode] + " " + value);
			//}
			
			if(!b) {
				super.analyseMultiByte(asv, amv, graph, m, list);
			}
		}
	}
}
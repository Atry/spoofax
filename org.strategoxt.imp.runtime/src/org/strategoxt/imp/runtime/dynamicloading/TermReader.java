package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.terms.IStrategoTerm.APPL;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;

import java.util.ArrayList;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Term reading utility class, extending {@link org.spoofax.interpreter.core.Tools}
 * with new methods specific for reading descriptors.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TermReader extends Tools {
	private TermReader() {
		// TermReader cannot be constructed
	}
	
	public static IStrategoAppl findTerm(IStrategoTerm term, String constructor) {
		if (term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
			return (IStrategoAppl) term;
		
		for (int i = 0; i < term.getSubtermCount(); i++) {
			IStrategoAppl result = findTerm(termAt(term, i), constructor);
			if (result != null) return result;
		}
		
		return null;
	}
	
	public static ArrayList<IStrategoAppl> collectTerms(IStrategoAppl term, String... constructors) {
		ArrayList<IStrategoAppl> results = new ArrayList<IStrategoAppl>();
		for (String constructor : constructors) {
			collectTerms(term, constructor, results);
		}
		return results;
	}
	
	private static void collectTerms(IStrategoTerm term, String constructor, ArrayList<IStrategoAppl> results) {
		if (term.getTermType() == IStrategoTerm.APPL && cons(term).equals(constructor))
			results.add((IStrategoAppl) term);
		
		for (int i = 0; i < term.getSubtermCount(); i++) {
			collectTerms(termAt(term, i), constructor, results);
		}
	}
	
	public static String termContents(IStrategoTerm t) {
		if (t == null) return null;
		
		String result;
		
		if (t.getTermType() == STRING) {
			result = asJavaString(t);
		} else if (t.getTermType() == APPL && t.getSubtermCount() == 1 && termAt(t, 0).getTermType() == STRING) {
			result = asJavaString(termAt(t, 0));
		} else if (t.getTermType() == APPL && t.getSubtermCount() == 1) {
			return termContents(termAt(t, 0));
		} else {
			return null;
		}
		
		if (result.startsWith("\"") && result.endsWith("\""))
			result = result.substring(1, result.length() - 1).replace("\\\\", "\"");
		
		return result;
	}

	public static String concatTermStrings(IStrategoTerm values) {
		IStrategoList list = termAt(values, 0);
		StringBuilder results = new StringBuilder();
		
		if (list.getSubtermCount() > 0)
			results.append(termContents(termAt(list, 0)));
		
		for (int i = 1; i <  list.getSubtermCount(); i++) {
			results.append(',');
			results.append(termContents(termAt(list, i)));
		}
		return results.toString();
	}
	
	public static int parseIntAt(IStrategoTerm t, int index) {
		return Integer.parseInt(termContents(t.getSubterm(index)));
	}
	
	public static String cons(IStrategoTerm t) {
		if (t == null || t.getTermType() != APPL)
			return null;
		return ((IStrategoAppl) t).getConstructor().getName();
	}
}

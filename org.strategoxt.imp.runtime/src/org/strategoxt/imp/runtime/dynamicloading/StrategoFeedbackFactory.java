package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.StrategoFeedback;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoFeedbackFactory extends AbstractServiceFactory<StrategoFeedback> {
	
	@Override
	public Class<StrategoFeedback> getCreatedType() {
		return StrategoFeedback.class;
	}
	
	@Override
	public StrategoFeedback create(Descriptor descriptor) throws BadDescriptorException {
		IStrategoAppl document = descriptor.getDocument();
		Interpreter interpreter;
		
		// TODO: Sharing of FeedBack instances
		
		try {
			interpreter = Environment.createInterpreter();
		} catch (Exception e) {
			Environment.logException("Could not create interpreter", e);
			return null;
		}
		
		for (IStrategoAppl term : makeSet(collectTerms(document, "SemanticProvider"))) {
			String filename = termContents(term);
			if (filename.endsWith(".ctree")) {
				try {
					Debug.startTimer("Loading Stratego module ", filename);
					interpreter.load(descriptor.openAttachment(filename));
					Debug.stopTimer("Successfully loaded " +  filename);
				} catch (InterpreterException e) {
					throw new BadDescriptorException("Error loading compiler service provider " + filename, e);
				} catch (IOException e) {
					throw new BadDescriptorException("Could not load compiler service provider" + filename, e);
				}
			} else {
				Debug.log("Not a compiler service provider, ignoring for now: ", filename);
			}
		}
		
		String observerFunction = descriptor.getProperty("SemanticObserver", null);
		
		return new StrategoFeedback(descriptor, interpreter, observerFunction);
	}
	
	private static<E> Set<E> makeSet(List<E> list) {
		// FIXME: Duplicates appear in descriptor files?
		//        Currently, I'm making a set of property values to eliminate duplicates
		//        to avoid this problem
		return new HashSet<E>(list);
	}

}
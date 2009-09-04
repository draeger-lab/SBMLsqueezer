/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.ontology; 
 
import org.biojava.bio.Annotatable;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioRuntimeException;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;

/**
 * Abstract implementation of term
 *
 * This provides basic change-forwarding functionality from
 *                the annotation and ontology properties.
 *
 * @author Thomas Down
 * @since 1.4
 */
 
public abstract class AbstractTerm extends AbstractChangeable implements Term {
    private transient ChangeForwarder forwarder;
    protected String description;
    
    public ChangeSupport getChangeSupport(ChangeType ct) {
            ChangeSupport cs = super.getChangeSupport(ct);
            forwarder = new ChangeForwarder(this, cs) {
                protected ChangeEvent generateEvent(ChangeEvent cev) {
                    if (cev.getSource() instanceof Ontology) {
                        return new ChangeEvent(
                            getSource(),
                            Term.ONTOLOGY,
                            getOntology(),
                            null,
                            cev
                        );
                    } else if (cev.getSource() instanceof Annotation) {
                        return new ChangeEvent(
                            getSource(),
                            Annotatable.ANNOTATION,
                            getAnnotation(),
                            null,
                            cev
                       );
                    } else {
                        throw new BioRuntimeException("Unknown event");
                    }
                }
            } ;
            getAnnotation().addChangeListener(forwarder, ChangeType.UNKNOWN);
            getOntology().addChangeListener(forwarder, ChangeType.UNKNOWN);
            return cs;
    }
    
    public  void setDescription(String description){
    	this.description = description;
    }
}

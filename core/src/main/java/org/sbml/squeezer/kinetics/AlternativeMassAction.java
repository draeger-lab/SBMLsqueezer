package org.sbml.squeezer.kinetics;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;
import org.sbml.jsbml.*;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;
import sun.security.krb5.internal.ASRep;

import javax.xml.stream.XMLStreamException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 */
public class AlternativeMassAction extends GeneralizedMassAction implements
        InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
        InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

    /**
     * @param parentReaction
     * @param types
     * @throws RateLawNotApplicableException
     * @throws XMLStreamException
     */
    public AlternativeMassAction(Reaction parentReaction, Object... types)
            throws RateLawNotApplicableException, XMLStreamException {
        super(parentReaction, types);
    }

    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    /**
     *
     * @param astNode
     * @param specRef
     */
    @SuppressWarnings("deprecation")
    private ASTNode createTerm(ASTNode astNode, SpeciesReference specRef) {
        if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
            ASTNode basis = speciesTerm(specRef);
            if (specRef.isSetStoichiometryMath()) {
                basis.raiseByThePowerOf(specRef.getStoichiometryMath().getMath().clone());
            } else if (specRef.isSetId() && (getLevel() > 2)) {
                // It might happen that there is an assignment rule that changes the stoichiometry.
                basis.raiseByThePowerOf(specRef);
            } else {
                double stoichiometry = specRef.getStoichiometry();
                if (stoichiometry != 1d) {
                    basis.raiseByThePowerOf(stoichiometry);
                    ASTNode exp = basis.getRightChild();
                    System.out.println("Else: " + stoichiometry);
                    if ((stoichiometry != 0d) && (getLevel() > 2) && !exp.isSetUnits()) {
                        // The right child must be the stoichiometric coefficient because we just set it as exponent.
                        exp.setUnits(Unit.Kind.DIMENSIONLESS);
                    }
                }
            }
            if(astNode == null) {
                return basis;
            }
            else {
                ASTNode clonedAstNode = astNode.clone();
                return clonedAstNode.multiplyWith(basis);
            }
        }
        return null;
    }

    /**
     *
     * @param catalysts
     * @param catNum
     * @return
     */
    @Override
    ASTNode association(List<String> catalysts, int catNum) {
        Reaction r = getParentSBMLObject();
        ASTNode multReac = null;
        for(SpeciesReference specRef: r.getListOfReactants()) {
            multReac = createTerm(multReac, specRef);
        }
        return multReac;
    }

    /**
     *
     * @param catalysts
     * @param catNum
     * @return
     */
    @Override
    ASTNode dissociation(List<String> catalysts, int catNum) {
        Reaction r = getParentSBMLObject();
        ASTNode kEq = new ASTNode(parameterFactory.parameterEquilibriumConstant(r));
        ASTNode multProd = null;
        for(SpeciesReference specRef:r.getListOfProducts()) {
            System.out.println("SpecRefProd: " + specRef);
            multProd = createTerm(multProd, specRef);
        }
        return ASTNode.frac(multProd, kEq);
    }


    @Override
    ASTNode createKineticEquation(List<String> modE, List<String> modActi,
                                  List<String> modInhib, List<String> modCat) {
        orderReactants = orderProducts = Double.NaN;
        List<String> catalysts = new LinkedList<String>(modE);
        catalysts.addAll(modCat);
        ASTNode rates[] = new ASTNode[Math.max(1, catalysts.size())];
        Reaction reaction = getParentSBMLObject();
        for (int c = 0; c < rates.length; c++) {
            rates[c] = association(catalysts, c);
            if (reaction.getReversible()) {
                rates[c].minus(dissociation(catalysts, c));
            }
            if (catalysts.size() > 0) {
                rates[c].multiplyWith(speciesTerm(catalysts.get(c)));
            }
            ASTNode kF = new ASTNode(parameterFactory.parameterAssociationConst(
                    catalysts.size() > 0 ? catalysts.get(c) : null));
            rates[c] = ASTNode.times(kF, rates[c]);
        }
        super.setSBOTerm();
        return ASTNode.times(activationFactor(modActi),
                inhibitionFactor(modInhib), ASTNode.sum(rates));
    }

    /* (non-Javadoc)
     * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
     */
    @Override
    public String getSimpleName() {
        return MESSAGES.getString("ALTERNATIVE_MASS_ACTION_SIMPLE_NAME");
    }
}

# SBMLsqueezer
<img align="right" src="resources/org/sbml/squeezer/resources/img/SBMLsqueezerIcon_64.png"/>

**Context-sensitive creation of kinetic equations in biochemical networks**

[![License (GPL version 3)](https://img.shields.io/badge/license-GPLv3.0-blue.svg?style=plastic)](http://opensource.org/licenses/GPL-3.0)
[![Latest version](https://img.shields.io/badge/Latest_version-2.1-brightgreen.svg?style=plastic)](https://github.com/draeger-lab/SBMLsqueezer/releases/)
[![DOI](http://img.shields.io/badge/DOI-10.1186%20%2F%20s12918-015-0212-9-blue.svg?style=plastic)](http://dx.doi.org/10.1186/s12918-015-0212-9)
[![Build Status](https://travis-ci.org/draeger-lab/SBMLsqueezer.svg?branch=master&style=plastic)](https://travis-ci.org/draeger-lab/SBMLsqueezer)

*Authors:* [Andreas Dräger](https://github.com/draeger/), [Sebastian Nagel](https://github.com/nagel86/), [Sarah R. Müller vom Hagen](https://github.com/mvhsara/), [Johannes Pfeuffer](https://github.com/jpfeuffer/), [Lisa Falk](https://github.com/LisaFalk/), [Thomas M. Hamm](https://github.com/tmHamm/), [Clemens Wrzodek](https://github.com/Clemens82/)
___________________________________________________________________________________________________________

### Short description
SBMLsqueezer generates kinetic equations for biochemical networks according to context of
each reaction. When used as a plug-in for CellDesigner it uses the information from the SBGN representation
of all network components. In the stand-alone mode, SBMLsqueezer evaluates the Systems Biology Ontology (SBO)
annotations to extract this information. An online version of SBMLsqueezer is available that runs without
instally any software on the local machine. The rate laws that can be produced by SBMLsqueezer include several
types of generalized mass action; detailed and generalized enzyme kinetics, various types of Hill equations,
S- and H-systems, and additive models for gene regulation. User defined settings specify which equation to
apply for any type of reaction and how to ensure unit consistency of the model. Equations can be created using
contextual menus. All newly created parameters are equipped with the derived unit and annotated with SBO terms
if available and meaningful textual names. MathML is inserted directly into the SBML file. LaTeX or text export
of ordinary differential equations is provided.

### Please cite

1. Andreas Dräger, Daniel C. Zielinski, Roland Keller, Matthias Rall, Johannes Eichner, Bernhard O. Palsson,
   and Andreas Zell. SBMLsqueezer 2: Context-sensitive creation of kinetic equations in biochemical networks.
   _BMC Systems Biology_, 9(1):1-17, September 2015. [ [DOI](http://dx.doi.org/10.1186/s12918-015-0212-9) |
   [link](http://dx.doi.org/10.1186/s12918-015-0212-9) | [pdf](http://www.biomedcentral.com/content/pdf/s12918-015-0212-9.pdf) ]
2. Andreas Dräger. _Computational Modeling of Biochemical Networks_. Ph.D. thesis, University of Tübingen,
   Tübingen, Germany, January 2011. [ [link](http://www.dr.hut-verlag.de/978-3-86853-850-2.html) ]
3. Andreas Dräger, Adrian Schröder, and Andreas Zell. Systems Biology for Signaling Networks, volume 1 of
   Systems Biology, chapter Automating mathematical modeling of biochemical reaction networks, pages 159-205.
   Springer-Verlag, July 2010. [ [DOI](http://dx.doi.org/10.1007/978-1-4419-5797-9_7) | [link](http://www.springerlink.com/content/n77k80h76vj17806) ]
4. Andreas Dräger, Nadine Hassis, Jochen Supper, Adrian Schröder, and Andreas Zell. SBMLsqueezer: a
   CellDesigner plug-in to generate kinetic rate equations for biochemical networks. BMC Systems Biology,
   2(1):39, April 2008. [ [DOI](http://dx.doi.org/10.1186/1752-0509-2-39) | [link](http://www.biomedcentral.com/1752-0509/2/39) | [pdf](http://www.biomedcentral.com/content/pdf/1752-0509-2-39.pdf) ]
   
## Users' Guide

For more information, see the [Users' Guide](http://vg08.met.vgwort.de/na/d4b8e9ae1bed423884cea07e52756b43?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/doc/SBMLsqueezer2.1UsersGuide.pdf)

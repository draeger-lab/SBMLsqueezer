About
----------------

SBMLsqueezer generates kinetic equations for biochemical networks according to context of each reaction.
When used as a plug-in for CellDesigner it uses the information from the SBGN representation of all network components.
In the stand-alone mode, SBMLsqueezer evaluates the Systems Biology Ontology (SBO) annotations to extract this information.
An online version of SBMLsqueezer is available that runs without instally any software on the local machine.
The rate laws that can be produced by SBMLsqueezer include several types of generalized mass action; detailed
and generalized enzyme kinetics, various types of Hill equations, S- and H-systems, and additive models
for gene regulation. User defined settings specify which equation to apply for any type of reaction
and how to ensure unit consistency of the model. Equations can be created using contextual menus.
All newly created parameters are equipped with the derived unit and annotated with SBO terms if available
and meaningful textual names. MathML is inserted directly into the SBML file. LaTeX or text export
of ordinary differential equations is provided.


<span class="figure">
   <img id="SBMLdemo" src="images/SBMLsqueezer_demonstration.png"/>
   <b>Figure 1</b> | SBMLsqueezer reaction context menu on the model <a href="http://bigg.ucsd.edu/models/iIT341" >iIT341</a> of Helicobacter pylori 26695
</span>

###Main program features

+ Generates kinetic equations for all reactions in your model, or only for those reactions
that are currently lacking a rate law with a large variety of generic and specific rate laws for several standard cases
+ Allows you to create all rate laws in a reversible manner.
+ Can detect reactive species, whose annotation indicates that these are genes.
+ Can assume that all reactions in your network are enzymatically catalyzed and hence change the selection of rate laws.
+ Defines the units of all species and compartments if necessary and derive the units
for all newly created local and global parameters and numbers in order to ensure unit consistency of the entire model.
+ Can check the model for global and local parameters as well as unit definitions
that are never used and addressed. SBMLsqueezer can automatically remove these from the model.
+ Imports experimentally determined rate equations from SABIO-RK and include them into your model.
+ Equips your model with default values where no values are defined.
+ Summarizes all features of the model in an exhaustive LaTeX-based model report

###User's guide and kinetic laws

| Document (pdf) | Size | Version |
| -------------- | ---- | ------- |
| [SBMLsqueezer 2.1 Users' Guide](http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/doc/SBMLsqueezer2.1UsersGuide.pdf) | 4.2 MB | 2015-08-10 |
| [SBMLsqueezer 2 Users' Guide](http://vg08.met.vgwort.de/na/d4b8e9ae1bed423884cea07e52756b43?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/doc/SBMLsqueezerUsersGuide.pdf) | 4.2 MB | 2014-06-29 |
| [SBMLsqueezer 1 Users' Guide](http://vg08.met.vgwort.de/na/45bad7bc6232453db73f28c701c4b738?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/doc/Tutorial.pdf) | 960 kB |  	2008-04-28 |
| [Revised Kinetic Laws](http://vg08.met.vgwort.de/na/83a1b26b8b22412dacbce13703f523e1?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/doc/KineticLaws2.pdf) | 729 kB | 2010-03-31 |
| [Kinetic Laws](http://vg08.met.vgwort.de/na/5c631cb521294d6a93aad6d0cdc1c70b?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/doc/KineticLaws.pdf) | 375 kB | 2008-04-28 |
###Publications

1. 	Andreas Dräger, Daniel C. Zielinski, Roland Keller, Matthias Rall, Johannes Eichner, Bernhard O. Palsson, and Andreas Zell.
SBMLsqueezer 2: Context-sensitive creation of kinetic equations in biochemical networks. BMC Systems Biology, 9(1):1--17, September 2015.
\[ [DOI](http://dx.doi.org/10.1186/s12918-015-0212-9) |
[pdf](https://bmcsystbiol.biomedcentral.com/track/pdf/10.1186/s12918-015-0212-9) \]
2. 	Andreas Dräger. Computational Modeling of Biochemical Networks. PhD thesis, University of Tuebingen, Tübingen, Germany, January 2011.
\[ [link](https://www.dr.hut-verlag.de/978-3-86853-850-2.html) \]
3. 	Andreas Dräger, Adrian Schröder, and Andreas Zell. Systems Biology for Signaling Networks,
volume 1 of Systems Biology, chapter Automating mathematical modeling of biochemical reaction networks,
pages 159--205. Springer-Verlag, July 2010. \[ [DOI](http://dx.doi.org/10.1007/978-1-4419-5797-9_7) \]
4. 	Andreas Dräger, Nadine Hassis, Jochen Supper, Adrian Schröder, and Andreas Zell. SBMLsqueezer:
a CellDesigner plug-in to generate kinetic rate equations for biochemical networks. BMC Systems Biology,
2(1):39, April 2008. \[ [DOI](http://dx.doi.org/10.1186/1752-0509-2-39) |
[pdf](http://www.biomedcentral.com/content/pdf/1752-0509-2-39.pdf) \]

**Poster and diploma theses**

+ Nadine Hassis (2007): Automatische Generierung kinetischer Gleichungen aus Stöchiometrien, Diplomarbeit, Universät Tübingen
\[ [pdf](http://vg08.met.vgwort.de/na/7e9224fd19d648c2b92f388520329751?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/publications/DiplomarbeitNHassis.pdf) \]
+ Andreas Dräger, Nadine Hassis, Jochen Supper, Adrian Schröder, Andreas Zell (2007): SBMLsqueezer:
a CellDesigner plug-in to generate kinetic rate equations for biochemical networks, poster (HepatoSys conference).
\[ [pdf](http://vg08.met.vgwort.de/na/0b36965b80bc4f66a9547ad630ee8f93?l=http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/publications/SBMLsqueezerA0.pdf)\]

**This project is promoted by:**

<table style="text-align:center;" width="95%" cellspacing="10" cellpadding="10" border="0" align="center">
<tbody>
<tr>
<td width="2%"><a href="https://www.dfg.de/en/index.jsp" target="_blank"><b>DFG</b></a></td>
<td width="2%"><a href="https://www.dzif.de/en" target="_blank"><b>DZIF</b></a></td>
<td width="2%"><a href="http://www.bmbf.de" target="_blank"><b>BMBF</b></a></td>

</tr>
<tr height="10">
<td width="2%">
<a class="image-link" href="https://www.dfg.de/en/index.jsp" target="_blank">
<img class="prom_logo" src="images/DFG.svg" title="DFG" alt="DFG"  /></a>
</td>
<td width="2%">
<a class="image-link" href="https://www.dzif.de/en" target="_blank">
<img class="prom_logo" src="images/DZIF.svg" title="DZIF" alt="DZIF" /></a>
</td>
<td width="2%">
<a class="image-link" href="http://www.bmbf.de" target="_blank">
<img class="prom_logo" src="images/BMBF_Logo_en.png" title="Federal Ministry for Education and Research" alt="Federal Ministry for Education and Research" /></a>
</td>
</tr>
</tbody>
</table>
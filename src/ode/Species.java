package ode;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class Species {

	public String fromSpeciesToTex(String specie)
	{
		int index = specie.length();
		for(int i = 0; i < 10; i++)
		{
			String j = "" + i;
			if(index > specie.indexOf(j) && specie.indexOf(j) > 0)
			{
				index = specie.indexOf(j);
			}
		}
		String name = specie.substring(0, index);
		String num = specie.substring(index);
		String speciesTex = "[" + name + "_{\\text{" + num + "}}]";
		
	    return speciesTex;
	}
}

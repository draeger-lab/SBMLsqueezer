package ode;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class CopyOfString2Tex2 {

	private String str;
	
	public void setInputString(String inputStr)
	{
		str=inputStr;
	}	
	
	public String getEquation (){
		
		String equation=str;
		equation = equation.replaceAll("_", "");
		
		while(equation.contains("/"))
		{
			equation = bruch(equation);
		}
		
		while(equation.contains("^("))
		{
			equation = hoch(equation);
		}

		return equation;
	}
	
	private String hoch(String equation)
	{
		String sub;
		int c = equation.indexOf("^(");
		sub = equation.substring(0,c+1); //pre-string with ^
		sub = sub + "{";
		int count= 0;
		for(int j = (c+1); j < equation.length(); j++)
		{	
			if(equation.charAt(j)==')')
			{
				if(count==0)
				{
					sub = sub + equation.substring(c+2,j) + "}";
					equation =  sub + equation.substring(j+1);
					break;
				}
				else
					count = count -1;
			}
			if(equation.charAt(j)==')')
				count = count +1;
		}
		return equation;
	}
	
	private String bruch( String equation)
	{
		String sub1;
		String sub2;
		String sub3_1 = "";
		String sub4_1 = "";
		String sub3 = null;
		String sub4 = null;

			int i;
			i = equation.indexOf("/");
			System.out.println("z72: index /:" + i);
			sub1 = equation.substring(0,i);
			sub2 = equation.substring((i+1));
			System.out.println("sub1: " + sub1);
			System.out.println("sub2: " + sub2);
			
			//numerator		
			while(sub1.charAt(i-1)==' ')
			{
				i = i-1;
				sub1 = sub1.substring(0 ,i);
			}
			if(sub1.charAt(i-1)==')')
			{
				int count= 0;
				int count1 = -1;
				for(int j = (i-2); j >= 0; j--)
				{	
					if(sub1.charAt(j)=='(')
					{
						count1 = count1 +1;
						if(count==0&&count1==0)
						{	
							sub3 = sub1.substring(j+1,i-1);
							if(j!=0)
								sub3_1 = sub1.substring(0,j);
							break;
						}
						if(count!=0){
							count = count -1;
						}
					}
					if(sub1.charAt(j)==')'){
						count = count + 1;
						count1 = count1 - 1;
					}
				}
			}
			else
			{
				for(int j = (i-2); j >= 0; j--)
				{
					if(sub1.charAt(j)=='+'||sub1.charAt(j)=='-'||sub1.charAt(j)=='*'||sub1.charAt(j)=='/'||sub1.charAt(j)=='(')
					{
						sub3 = sub1.substring(j+1);
						sub3_1 = sub1.substring(0,j+1);
						System.out.println("z113: sub3: "+sub3);
						System.out.println("z114: sub3_1: "+sub3_1);
					break;
					}
					if(j==0)
						sub3 = sub1.substring(0);
				}
			}
			sub3 = sub3_1 + "\\frac{" + sub3 + "}{";
			//denumerator
			while(sub2.charAt(0)==' ')
				sub2 = sub2.substring(1);
			if(sub2.charAt(0)=='(')
			{
				System.out.println("z123: sub2: ");
				int count= 0;
				for(int j = 1; j < sub2.length(); j++)
				{	
					if(sub2.charAt(j)==')')
					{
						if(count==0)
						{
							sub4 = sub2.substring(0,j+1);
							System.out.println("z139:sub4: " + sub4);
							if(j!=(sub2.length()-1))
							{
								sub4_1 = sub2.substring(j+1);
								System.out.println("z143:sub4_1: " + sub4_1);
							}
							break;
						}
						else
							count = count -1;
					}
					if(sub2.charAt(j)=='(')
						count = count +1;
				}
			}
			else
			{
				for(int j = 0; j < sub2.length(); j++)
				{
					if(sub2.charAt(j)=='+'||sub2.charAt(j)=='-'||sub2.charAt(j)=='*'||sub2.charAt(j)=='/'||sub2.charAt(j)==')')
					{
						sub4= sub2.substring(0,j);
						if(j!=(sub2.length()-1))
						{
							if(sub2.charAt(j)==')')
								sub4_1 = sub2.substring(j);
							else
								sub4_1 = sub2.substring(j);
						}
						break;
					}
					if(j==(sub2.length()-1))
						sub4 = sub2.substring(0);
				}
			}
			sub4 = sub4 + "}" + sub4_1;
			
			equation =  sub3 + sub4;
		return equation;
	}
	
	public static void main(String args[]){    
		String eq1 ="(kcatp_6_0 * s31 * s23 * s22 / (kIr1_6_0 * kMr2_6_0) - kcatn_6_0 * s31 * s24 / kMp1_6_0) / (1 + s23 / kIr1_6_0 + kMr1_6_0 * s22 / kIr1_6_0 * kMr2_6_0 + s23 * s22 / kIr1_6_0 * kMr2_6_0 + kMr1_6_0 * s22 * s24 / kIr1_6_0 * kMr2_6_0 * kIp1_6_0 + s24 / kMp1_6_0)";
		
       /* String eq2 ="((a+b)*c)/b";
        String eq3 ="jgjgjg(jjjj)(*(-alpha))/(bbeta+ceta)";
        String eq4 ="(b/3 + b/3)/(1+a/b)";
        String eq5 ="alpha+betA^(3++33+4)";
        String eq6 ="aALPHA+bbeta^333*666+444";
		*/
		CopyOfString2Tex2 s2t = new CopyOfString2Tex2();
        
		s2t.setInputString(eq1);
		System.out.println(s2t.getEquation());
    /*	s2t.setInputString(eq2);
		System.out.println(s2t.getEquation());
		s2t.setInputString(eq3);
		System.out.println(s2t.getEquation());
		s2t.setInputString(eq4);
		System.out.println(s2t.getEquation());
		s2t.setInputString(eq5);
		System.out.println(s2t.getEquation());
		s2t.setInputString(eq6);
		System.out.println(s2t.getEquation());
    */
	}
}







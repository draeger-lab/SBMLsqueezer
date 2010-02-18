package ode;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class GaussRang {

	private double[][] N;
	private int n;
	private int m;
	public boolean vollerRang;
	
	public GaussRang(double[][] N, int n, int m)
	 {
		 this.n=n;
		 this.m=m;
		 this.N = N;
		 zeilenstufenform();
		 vollerRang();
	 }
	
	private void vertausche(int i, int j){
		for(int k = 0; k < m; ++k){ // vertausche Zeilen i und j.
			double temp = N[i][k];
			N[i][k] = N[j][k];
			N[j][k] = temp;
			}
		}
	
	private void zeilenstufenform(){
		int i = 0, j = 0;
		zst_rek(i,j);
		}
	
	private void zst_rek(int i,int j){
		if(i == n-1 || j >= m) // Abbruchbedingung
			return;
		int piv = pivotsuche(i,j); // suche Pivotelement unterhalb des Element i,j
		if (N[piv][j] == 0){ // kein Pivotelement != 0 gefunden
			zst_rek(i,j+1); // gleiche Zeile, nächste Spalte
			return;
		}
		vertausche(i,piv); // vertausche Zeile i mit Zeile piv
		for(int ii = i+1; ii < n ; ++ii)
		{
			double d = (double)-N[ii][j]/N[i][j];
			addiere(d,i,ii);
		}// mache j. Spalte unter i.ter Zeile zu 0
		zst_rek(i+1,j+1); // nächste Zeile, nächste Spalte
		return;
		}
	
	private void addiere (double la, int i, int j){
		for(int k = 0; k < m; ++k){ // la-faches der Zeile i zur Zeile j.
			N[j][k] += N[i][k]*la;
			}
		}
		
	private int pivotsuche(int i, int j){
		int piv = i;
		for(int k = i; k < n; k++)
			if((N[k][j]) > N[piv][j])
				piv = k;
		return piv;
		}
			
	void ausgabe(){
		for (int i = 0; i < n; ++i)
		{
			for (int j = 0; j < m; ++j)
				System.out.println( N[i][j]);
		}
	}
	
	private void vollerRang()
	{
		int c=0;
		for (int i = 0; i < n; ++i)
		{	
			int d=0;
			for (int j = 0; j < m; ++j)
			{
				if(N[i][j]!=0)
					d=1;
			}
			if(d==1)
				c= c+1;
		}
		if(c==m)
			vollerRang=true;
		else
			vollerRang=false;
	}
	
	public boolean getVollerRang()
	{
		return vollerRang;
	}
	
	
	
	
	
}

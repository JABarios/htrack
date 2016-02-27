class arr_int {
  int[] v1;
  int nelem;
  public arr_int(int n) {
    v1=new int[n];
    nelem=n;
  }
  
  public arr_int(int[] v2) {
    v1=v2;
    nelem=v1.length;
  }
  
  void set(int nn, int valor) {
    v1[nn]=valor;
  };
  int  get(int nn) {
    return(v1[nn]);
  };
  int length() {
    return(v1.length);
  };
  int[] toVInt() {
    return(v1);
  };

 int[] mult(int n) {
    for (int i=0; i<nelem; i++)v1[i]*=n;
    return(v1);
  }
 int[] zscore() {
    int media=avg(); 
    int desv=std(); 
    
    for (int i=0; i<nelem; i++)v1[i]-=media;
    for (int i=0; i<nelem; i++)v1[i]/=desv;

    return(v1);
  }
 int[] remove_offset() {
    int media=avg(); 
    
    for (int i=0; i<nelem; i++)v1[i]-=media;
    return(v1);
  }
  
 
  int sum_x1() {
    int suma=0;
    for (int i=0; i<nelem; i++)suma += v1[i];
    return(suma);
  }
  int sum_x2() {
    int suma=0;
    for (int i=0; i<nelem; i++)suma += (v1[i]*v1[i]);
    return(suma);
  }
  int avg() {
    return(sum_x1()/nelem);
  };
  int std() {
    int mm=avg();
    return((int)sqrt(int(sum_x2() / nelem) - (mm * mm)));
  }

  int[] trocito(int principio, int fin) {
    int [] salida=new int[fin-principio+1];
    for (int i=0; i<(fin-principio+1); i++) {
      salida[i]=v1[principio+i];
    }
    return(salida);
  }

  // findpeaks simplificado, nivel sd*4/5, pone marcador en el maximo tras  pasar el nivel  y espera
  // para marcar otro hasta que la onda baje de nivel*2/3, pasado al menos "paso"
  // ahora funciona bien también con ondas cuadradas, y con sinusoidal lenta
  // apunta los tiempos en marcadores, que se pasa como parametro
  int busca_picos(int paso, int[] marcadores) {
    int npicos=0;
    for (int x = 0; x < marcadores.length; x++) { 
      marcadores[x]=0;
    }
    int nivel=avg()+std()*4/5;
    for (int x = 0; x < length() && npicos<200; x++) { 
      if (v1[x]>nivel) {
        int maximo=v1[x];
        // ahora xfin avanza desde x+paso (paso es un "periodo refractario") hasta que encuentre un valor bajito, o hasta que se acabe el buffer
        int xfin=x+paso; 
        while(xfin<length() && v1[xfin%length()]>nivel*2/3){xfin++;};
        // en dicho intervalo, buscamos el maximo
        for (int y=x; y<xfin; y++) {
          if (v1[y%length()]>maximo) {
            maximo=v1[y%length()];
            x=y;
          }
        }  
        if(x<length())marcadores[npicos++]=x;
        x=xfin;
      }
    }
    return(npicos);
  }



}
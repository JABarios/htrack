package processing.test.mbvisorpc2;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import android.content.Intent; 
import android.os.Bundle; 
import ketai.net.bluetooth.*; 
import ketai.ui.*; 
import ketai.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MBvisorpc2 extends PApplet {

/*
(c) Juan A. Barios, Agosto 2015
 */







// Boooleanos, habria que armar una maquina de estados
boolean isConfiguring = true;
boolean isUI = false;         // pantalla UI vs pantalla REC
boolean isBT = true;          // Bluetooth conectado o no
boolean isBTiniciado=false;   //   
boolean isUIiniciado=false;   // 



boolean bReleased = true; //no permament sending when finger is tap
KetaiBluetooth bt;
String info = "";
KetaiList klist;
ArrayList devicesDiscovered = new ArrayList();
KetaiVibrate vibe;

int maxADC=1024;
int tasaRefresc=100;
int longEEG=512;
int longPROM=128;
float incTiempo=1.0f/tasaRefresc;

String myString = null;
String linea_leida = null;
String resto_linea = "";

int saltitos=0;

ventana VEEG, VPROM, VFFT;
c_arr_int  BEEG, BFFT;
avg_arr_int   BPROM, BPROM_FFT;

int marcadores;

// interfaz
PFont font;
PFont fontMy;
int Valtura=150;
int anchoPantalla=600;
int altoPantalla=400;
int anchoTeclado=anchoPantalla-80; // teclado virtual del UI 

//scrollbar
int anchoBarra=120;                   
int altoBarra=35;                   
Scrollbar scaleBar, scaleBar2;
float zoom;

//fft 512, tablas que se precalculan para acelerar la fft
double[] cosTable = new double[512 / 2];
double[] sinTable = new double[512 / 2];
int fft_iniciada=0;

//debug
float t=0;
float senal_prueba=0;
int cont=0;

public void setup() {
   // Stage size, representados en altoPantalla y anchoPantalla, pero no se puede fijar din\u00e1micamente
  // porque hay lio en ketai. La orientaci\u00f3n la dejo fijada en el archivo xml (portrait)

  background(0);
  frameRate(tasaRefresc);  
  textSize(28);
  textAlign(CENTER);
  rectMode(CENTER);
  ellipseMode(CENTER);  
  //font = loadFont("Arial-BoldMT-24.vlw");
  //textFont(font);
  BEEG=new c_arr_int(longEEG);
  BPROM=new avg_arr_int(longPROM);
  BFFT=new c_arr_int(longEEG/2);
  BPROM_FFT=new avg_arr_int(longEEG/2);

  isBTiniciado=false;

  KetaiAlertDialog.popup(this, "Memboost", "Memboost v 0.1, (c) JA Barios");
  inicia_ad();
}  

public void draw() {
  double[] espectro_EEG;

  if (isUI) {
    drawUI();
    isUIiniciado=false;
    return;
  } else if (isConfiguring && isBT) {
    ArrayList names;
    background(78, 93, 75);
    klist = new KetaiList(this, bt.getPairedDeviceNames());
    isConfiguring = false;
    isUIiniciado=false;
  } else {
    if (!isUIiniciado) {
      drawREC();
      isUIiniciado=true;
    }    
    t=t+incTiempo; 
    cont=cont+1;
    adquiere();
    VEEG.pintaVector(BEEG.get(), zoom, 0, 1024);
    if (cont%10==0) {
      promediando_EEG(10);
      VPROM.pintaVector(BPROM.get(), zoom, 0, 1024);
      VPROM.cursor(-0.5f);
      BPROM.reset();
      espectro_EEG = (BEEG.get_c()).espectro();
      BPROM_FFT.put(espectro_EEG);
    };   
    if (cont%200==0) {
      VFFT.pintaVector((BPROM_FFT.get()), zoom*4, 30, -5);
      BPROM_FFT.reset();
    };

    if (cont%5==0) {
      pinta_barrapie();
//      scaleBar.update (mouseX, mouseY);
//      scaleBar.display();
//      zoom = scaleBar.getPos();
       zoom=0.35f;
    }
  }
}

public void promediando_EEG(int paso) {
  int[] marcas=new int[200];
  arr_int senal=BEEG.get_c();
  int[] linea;
  int npuntos;

  npuntos=BPROM.length();
  marcadores=senal.picos(paso, marcas);

  for (int i=0; i<marcadores; i++) {
    if ((marcas[i]-npuntos/2)<0 || (marcas[i]+npuntos/2)>=senal.length()) {
      continue;
    }  
    linea=senal.trocito(marcas[i]-longPROM/2, marcas[i]+longPROM/2);
    BPROM.put(linea);
  }
}
// inicia el BT (que ya debe estar pareado)
public void inicia_ad() {
  senal_prueba=0;
  if (isBT) {
    bt.start();
    isConfiguring = true;
    linea_leida="";
  }
}

// rellena BEEG con lo que hay en linea_leida (si estamos conectados), o con una se\u00f1al de prueba si no.
public void adquiere() {
  if (!isBT) {
    senal_prueba=(20+20*sin(6.28f*4*t))+random(19)+20+(20*sin(6.28f*13*t));
    BEEG.put((int)senal_prueba*20);
  } else {
    parse_linea_leida(); //
  }
}

// procesa el buffer global linea_leida y lo vac\u00eda al acabar.
// formato: num1,num2,millis();
// no usamos los millis, a\u00fan
public void parse_linea_leida() {
  if (linea_leida.lastIndexOf(';')<0)return; // si linea a\u00fan incompleta, nos vamos
  String lines[] = linea_leida.split(";");
  String numeros[] ;
  for (int i=0; i<lines.length; i++) {
    numeros = lines[i].split(",");
    int valor=(PApplet.parseInt(numeros[1])); // el primer valor no se usa, se lo traga el BT no s\u00e9 por qu\u00e9 (bug)
    BEEG.put(PApplet.parseInt(numeros[1]));
    // no usamos los millis, a\u00fan
  }
  linea_leida="";
}

// Call back method to manage data received
// lee los datos que llegan por BT. Les a\u00f1ade lo que qued\u00f3 de la llamada anterior (resto_linea, global),
// y pasa al buffer global linea_leida todo hasta el ultimo punto y coma. El resto se almacena en
// resto_linea, hasta la siguiente llegada de datos BT
public void onBluetoothDataEvent(String who, byte[] data) {
  if (isConfiguring)
    return;
  if (!isBT)
    return;

  info = new String(data);
  info = trim(resto_linea)+trim(info);
  int nn=info.lastIndexOf(';');
  if (nn<0) {
    linea_leida = linea_leida+info;
    return;
  } else { 
    linea_leida=linea_leida+info.substring(0, nn);
    resto_linea=info.substring(nn+1);
  }
}


public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  bt = new KetaiBluetooth(this);
}

public void onKetaiListSelection(KetaiList klist) {
  String selection = klist.getSelection();
  bt.connectToDeviceByName(selection);
  //dispose of list for now
  klist = null;
}



// supongo que esto lo necesita la libreria ketai
public void onActivityResult(int requestCode, int resultCode, Intent data) {
  bt.onActivityResult(requestCode, resultCode, data);
}

class arr_int {
  int[] v1;
  int nelem;
  public arr_int(int n) {
    v1=new int[n];
    nelem=n;
  }
  public void set(int nn, int valor) {
    v1[nn]=valor;
  };
  public int  get(int nn) {
    return(v1[nn]);
  };
  public int length() {
    return(v1.length);
  };
  public int[] toVInt() {
    return(v1);
  };

  public int sum_x1() {
    int suma=0;
    for (int i=0; i<nelem; i++)suma += v1[i];
    return(suma);
  }
  public int sum_x2() {
    int suma=0;
    for (int i=0; i<nelem; i++)suma += (v1[i]*v1[i]);
    return(suma);
  }
  public int avg() {
    return(sum_x1()/nelem);
  };
  public int std() {
    int mm=avg();
    return((int)sqrt(PApplet.parseInt(sum_x2() / nelem) - (mm * mm)));
  }
  
  public int[] trocito(int principio,int fin) {
    int [] salida=new int[fin-principio+1];
    for(int i=0;i<(fin-principio+1);i++){
      salida[i]=v1[principio+i];
    }
    return(salida);
  }
  
  public int picos(int paso, int[] marcadores) {
    int npicos=0;
    for (int x = 0; x < marcadores.length; x++) { 
      marcadores[x]=0;
    }
    int nivel=avg()+std()*4/5;
    for (int x = 0; x < length() && npicos<200; x++) { 
      if (v1[x]>nivel) {
        int maximo=v1[x];
        for (int y=x; y<x+paso; y++) {
          if (v1[y%length()]>maximo) {
            maximo=v1[y%length()];
            x=y;
          }
        }  
        marcadores[npicos]=x;
        x+=paso;
        npicos++;
      }
    }
    return(npicos);
  }


public double[] fft() {
  double[] vv1=new double[nelem]; // solo funciona si nelem es potencia de 2
  double[] vvceros=new double[nelem];
  // vamos a restar la media, es mejor en esta aplicacion
  int suma=0;
  for (int i=0; i<v1.length; i++) suma += v1[i];
  for (int i=0; i<nelem; i++)vv1[i]=(double)(v1[i]-(suma/v1.length));
  //fftpot2((vv1), (vvceros));
  fft512((vv1), (vvceros));
  
  return(vv1);
}
public double[] espectro() {
  // log del cuadrado de la fft, con la mitad de puntos
  double[] vv1;
  double[] vv2=new double[nelem/2];
  vv1=fft();   
  for (int i=0; i<vv1.length/2; i++){
    vv2[i]=(double)Math.log((double)Math.pow((float)vv1[i], 2.0f));
  }
  return(vv2);
 }
}


/* 
 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
 * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
 * sacada del intenet
 */
public static void fftpot2(double[] real, double[] imag) {
  // Initialization
  if (real.length != imag.length)
    throw new IllegalArgumentException("Mismatched lengths");
  int n = real.length;
  int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
  if (1 << levels != n)
    throw new IllegalArgumentException("Length is not a power of 2");
  double[] cosTable = new double[n / 2];
  double[] sinTable = new double[n / 2];
  for (int i = 0; i < n / 2; i++) {
    cosTable[i] = Math.cos(2 * Math.PI * i / n);
    sinTable[i] = Math.sin(2 * Math.PI * i / n);
  }

  // Bit-reversed addressing permutation
  for (int i = 0; i < n; i++) {
    int j = Integer.reverse(i) >>> (32 - levels);
    if (j > i) {
      double temp = real[i];
      real[i] = real[j];
      real[j] = temp;
      temp = imag[i];
      imag[i] = imag[j];
      imag[j] = temp;
    }
  }

  // Cooley-Tukey decimation-in-time radix-2 FFT
  for (int size = 2; size <= n; size *= 2) {
    int halfsize = size / 2;
    int tablestep = n / size;
    for (int i = 0; i < n; i += size) {
      for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
        double tpre =  real[j+halfsize] * cosTable[k] + imag[j+halfsize] * sinTable[k];
        double tpim = -real[j+halfsize] * sinTable[k] + imag[j+halfsize] * cosTable[k];
        real[j + halfsize] = real[j] - tpre;
        imag[j + halfsize] = imag[j] - tpim;
        real[j] += tpre;
        imag[j] += tpim;
      }
    }
    if (size == n)  // Prevent overflow in 'size *= 2'
      break;
  }
}

// la fft de arriba, optimizada para buffer de 512. No comprueba tama\u00f1os de arrays, y precalcula las tablas. 
// usa un global fft_iniciada, que probablemente deberia ser un static, pero no controlo c\u00f3mo se hace
// esto deberia ser un metodo estatico, pero tampoco lo controlo, y da error de compilaci\u00f3n
public  void fft512(double[] real, double[] imag) {
  int n = 512;
  int levels=9;
  if(fft_iniciada==0){
    for (int i = 0; i < n / 2; i++) {
      cosTable[i] = Math.cos(2 * Math.PI * i / n);
      sinTable[i] = Math.sin(2 * Math.PI * i / n);
    }
    fft_iniciada=1;
  }  

  // Bit-reversed addressing permutation
  double temp=0;
 for (int i = 0; i < n; i++) {
    int j = Integer.reverse(i) >>> (32 - levels);
    if (j > i) {
      temp = real[i];
      real[i] = real[j];
      real[j] = temp;
      temp = imag[i];
      imag[i] = imag[j];
      imag[j] = temp;
    }
  }

  // Cooley-Tukey decimation-in-time radix-2 FFT
  for (int size = 2; size <= n; size *= 2) {
    int halfsize = size / 2;
    int tablestep = n / size;
    for (int i = 0; i < n; i += size) {
      for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
        double tpre =  real[j+halfsize] * cosTable[k] + imag[j+halfsize] * sinTable[k];
        double tpim = -real[j+halfsize] * sinTable[k] + imag[j+halfsize] * cosTable[k];
        real[j + halfsize] = real[j] - tpre;
        imag[j + halfsize] = imag[j] - tpim;
        real[j] += tpre;
        imag[j] += tpim;
      }
    }
    if (size == n)  // Prevent overflow in 'size *= 2'
      break;
  }
}
// clase que promedia los vectores int o double que le dan, y devuelve vector int. Ojo al truncado, pasa a int.
// metodos: put, get, length, reset;
class avg_arr_int {
  int[] v1, v2; // v1 lleva la suma total de lo a\u00f1adido, v2 lleva v1/num_promedios
  int num_promedios;
  boolean modificado;
  
  public avg_arr_int(int n) { 
    v1=new int[n]; // lleva la suma total de lo a\u00f1adido
    v2=new int[n]; // lleva v1/npromedios   
    reset();
  }
  public int length() {
    return(v1.length);
  };

  public void put(int[] x) {
    num_promedios++;
    modificado=true;
    for (int i=0; i<length(); i++)
      v1[i]+=x[i];
  };
  public void put(double[] x) {
    num_promedios++;
    modificado=true;
    for (int i=0; i<length(); i++)
      v1[i]+=(int)x[i];
  };

  public int[] get() {
    if (modificado){
       modificado=false;  
       for (int i=0; i<v1.length; i++)
         v2[i]=v1[i]/num_promedios;
    }    
    return v2;
  }
  public void reset() {
    num_promedios=0;
    modificado=false;
    for (int i=0; i<v1.length; i++) {
      v1[i]=0;v2[i]=0;
    }
  };
}
// array circular
// metodos put, 
class c_arr_int {
  arr_int v1;
  int puntero=0;
  public c_arr_int(int n) { 
    v1=new arr_int(n);
  }
  public int length() {
    return(v1.length());
  };

  public void put(int valor) {
    puntero++;
    v1.set(puntero%v1.length(), valor);
  };
  public void put_n(int pp, int valor) {
    puntero++;
    v1.set(pp%v1.length(), valor);
  };
  public int last() {
    return(v1.get((puntero)%v1.length()));
  };
  public arr_int get_c() {
    arr_int v2=new arr_int(v1.length());
    for (int i=0; i<v1.length(); i++) {
      v2.set(i, v1.get((puntero+i)%v1.length()));
    }
    return v2;
  }
  public int[] get() {
    int[] v2=new int[v1.length()];
    for (int i=0; i<v1.length(); i++) {
      v2[i]=v1.get((puntero+i)%v1.length());
    }
    return v2;
  }
  public void reemplazar(int[] x ) {
    puntero=0;
    for(int i=0;i<x.length;i++)
      v1.set(i,x[i]);
  };

}

public void mousePressed() {
  // en ambas pantallas, el borde derecho cambia de modo
  if ( mouseX > anchoPantalla-80) {
    isUI=!isUI;
    return;
  }   
  // en modo UI, teclado sencillo que llama distintas funciones
  if (isUI && mouseY < 100)
  {
    if (mouseX < anchoTeclado/3) {
      isConfiguring = !isConfiguring;
      isBT=!isBT;
      isUI=false;
      return;
    } else if (mouseX > anchoTeclado/3 && mouseX < anchoTeclado*2/3) {
      byte[] data = {'c'};                 // cambiar onda
      bt.broadcast(data);
    } else {
      KetaiAlertDialog.popup(this, "Memboost", "Memboost v 0.1, (c) JA Barios");
      return;
    }
  }
  if (isUI && (mouseY > 100 && mouseY < 200) )
  {
    if (mouseX < anchoTeclado/3) {
      byte[] data = {'a'};                 // reset frec
      bt.broadcast(data);
      return;
    } else if (mouseX > anchoTeclado/3 && mouseX < anchoTeclado-(anchoTeclado/3)) {
      byte[] data = {'k'};                 // inc frec
      bt.broadcast(data);
      return;
    } else {
      byte[] data = {'b'};                 // dec frec
      bt.broadcast(data);
      return;
    }
  }
  // scaleBar.press(mouseX, mouseY);
}

public void mouseReleased() {
  scaleBar.release();
}

public void keyPressed() {

  switch(key) {
  case 's':    // pressing 's' or 'S' will take a jpg of the processing window
  case 'S':
    saveFrame("Memboost-####.jpg");    // take a shot of that!
    break;

  default:
    break;
  }
}


public void pinta_textos() {
  fill(color(255, 253, 248));                            
  text("Memboost EEG Visualizer 0.1a", width/2, 20);     
}

public void pinta_borde() {
  fill(color(255, 0, 0));
  rect(anchoPantalla-40,altoPantalla/2,80,altoPantalla);
}


public void pinta_barrapie() {
  textSize(25);
  textAlign(CENTER);
  fill(255);
  rect(width/2, altoPantalla-12, width, 32);
  fill(0);
  text(""+nf(tasaRefresc, 2)+" "+nf(marcadores, 2), anchoPantalla/2, altoPantalla-8);
  if(!isBT)fill(color(255,0,0));
  else fill(color(255,255,0));
  rect(16, altoPantalla-12, 32, 32);
  text("BT", 16, altoPantalla-16);
}


public void drawUI()
{
  pushStyle();
  background(0);
  textAlign(CENTER);
  fill(0);
  stroke(255);
  int offset=anchoTeclado/6;
  rect(offset, 60, anchoTeclado/3, 120);
  rect(anchoTeclado/3+offset, 60, anchoTeclado/3, 120);
  rect((anchoTeclado/3)*2+offset, 60, anchoTeclado/3, 120);
  rect(offset, 160, anchoTeclado/3, 120);
  rect(anchoTeclado/3+offset, 160, anchoTeclado/3, 120);
  rect((anchoTeclado/3)*2+offset, 160, anchoTeclado/3, 120);

  fill(255);
  if(isBT){
      text("Simula", offset, 60);
  }else{
      text("BT", offset, 60);
  }
  text("Chg Onda", offset+anchoTeclado/3, 60); 
  text("About", offset+anchoTeclado/3*2, 60);
  text("Reset", offset, 160);
  text("Acelera", offset+anchoTeclado/3, 160); 
  text("Frena", offset+anchoTeclado/3*2, 160);
  
  
  popStyle();
  pinta_borde(); 
}

public void drawREC()
{
  background(0,255,255);
  pinta_textos(); 
  VEEG=new ventana(width/2, 10+height/5, longEEG, 128);
  VPROM=new ventana(width/4, height/3*2, longPROM, Valtura );
  VFFT=new ventana(width/4*3, height/3*2, longEEG/2, Valtura );
  scaleBar = new Scrollbar (VFFT.mrgD()-anchoBarra/2, 
     400-altoBarra/2, anchoBarra, altoBarra, 0.05f, 1.0f);
  pinta_borde();
}

/*
    THIS SCROLLBAR OBJECT IS BASED ON THE ONE FROM THE BOOK "Processing" by Reas and Fry
*/

class Scrollbar{
 int x,y;               // the x and y coordinates
 float sw, sh;          // width and height of scrollbar
 float pos;             // position of thumb
 float posMin, posMax;  // max and min values of thumb
 boolean rollover;      // true when the mouse is over
 boolean locked;        // true when it's the active scrollbar
 float minVal, maxVal;  // min and max values for the thumb
 
 Scrollbar (int xp, int yp, int w, int h, float miv, float mav){ // values passed from the constructor
  x = xp;
  y = yp;
  sw = w;
  sh = h;
  minVal = miv;
  maxVal = mav;
  pos = x - sh/2;
  posMin = x-sw/2;
  posMax = x + sw/2;  // - sh; 
 }
 
 // updates the 'over' boolean and position of thumb
 public void update(int mx, int my) {
   if (over(mx, my) == true){
     rollover = true;            // when the mouse is over the scrollbar, rollover is true
   } else {
     rollover = false;
   }
   if (locked == true){
    pos = constrain (mx, posMin, posMax);
   }
 }

 // locks the thumb so the mouse can move off and still update
 public void press(int mx, int my){
   if (rollover == true){
    locked = true;            // when rollover is true, pressing the mouse button will lock the scrollbar on
   }else{
    locked = false;
   }
 }
 
 // resets the scrollbar to neutral
 public void release(){
  locked = false; 
 }
 
 // returns true if the cursor is over the scrollbar
 public boolean over(int mx, int my){
  if ((mx > x-sw/2) && (mx < x+sw/2) && (my > y-sh/2) && (my < y+sh/2)){
   return true;
  }else{
   return false;
  }
 }
 
 // draws the scrollbar on the screen
 public void display (){
   if(1==1)return;
  noStroke();
  fill(0); //creado un marco, para limpiar los alrededores
  fill(78, 93, 75);

  rect(x-40, y-5, sw+130, sh+20);     
 
  fill(255);
  rect(x, y, sw, sh);      // create the scrollbar
 
  fill (250,0,0);
  if ((rollover == true) || (locked == true)){             
   stroke(250,0,0);
   strokeWeight(8);           // make the scale dot bigger if you're on it
  }
  
  ellipse(pos, y, sh, sh);     // create the scaling dot
  strokeWeight(1);            // reset strokeWeight
 }
 
 // returns the current value of the thumb
 public float getPos() {
  float scalar = sw / sw;  // (sw - sh/2);
  float ratio = (pos-(x-sw/2)) * scalar;
  float p = minVal + (ratio/sw * (maxVal - minVal));
  return p;
 } 
 }
 
public static boolean isVacia(String param) {
  if (param==null)return true;
  if (param.trim().equals(""))return true;
  return false;
}
class ventana {
  int _ctro_x;public int cx(){return(_ctro_x);}
  int _ctro_y;public int cy(){return(_ctro_y);}
  int _mrgI;public int mrgI(){return(_mrgI);}
  int _mrgD;public int mrgD(){return(_mrgD);}
  int _mrgS; public int mrgS(){return(_mrgS);}
  int _mrgF;public int mrgF(){return(_mrgF);}
  int _ancho;public int ancho(){return(_ancho);}
  int _alto;public int alto(){return(_alto);}
  int _fondo;public int fondo(){return(_fondo);}
  

  // constructor, calcula coordenadas y pinta fondo de color
  ventana(int p_cx, int p_cy, int p_ancho, int p_alto) {
    _ctro_x=p_cx; _ctro_y=p_cy;
    _ancho=p_ancho;_alto=p_alto;
    _mrgI=_ctro_x-(_ancho/2);
    _mrgD=_ctro_x+_ancho/2;
    _mrgS=_ctro_y-_alto/2;
    _mrgF=_ctro_y+_alto/2;
    _fondo = color(255, 253, 248);

    dibuja_fondo();
    }
  
  // dibuja de color _fondo la ventana
  public void dibuja_fondo(){
      noStroke();
      fill(_fondo);  // color for the window background
      rect(_ctro_x, _ctro_y, _ancho, _alto);
      
  }
  // pinta cursor, si n<0, con valor relativo (-0.5, justo en la mitas), si n>0 en cx=n 
  public void cursor(float n){
     fill(255, 0, 0);  // color for the window background
     if(n<0){
       float cx=(float)_ancho*n;
       rect((int)cx,_mrgI, 1,_alto );
     } else {
       rect(n,_ctro_y, 1,_alto );
     }
     
  }
  
    // 4 funciones, cambiando el tipo de los parametros de entrada, que pintan un vector en la ventana
    // incluyen limpiar el fondo antes de dbujar
    public void pintaVector(int[] x1,float escala) {
      float cx1,cy1;
      dibuja_fondo();
      stroke(250, 0, 0);     //red                          
      noFill();
      beginShape();             
      for (int i = 0; i < x1.length; i++) {
        cx1=(float)map(i,0,x1.length,mrgI(),mrgD());
        cy1=cy()+escala*x1[i];
        cy1=cy1<mrgS()?mrgS():cy1;
        vertex(cx1,  cy1>mrgF()?mrgF():cy1);
      }
      endShape();
    }

    public void pintaVector(int[] x1,float escala, int minimo, int maximo) {
      float cx1,cy1;
      dibuja_fondo();
      stroke(250, 0, 0);     //red                          
      noFill();
      beginShape();             
      for (int i = 0; i < x1.length; i++) {
        cx1=(float)map(i,0,x1.length,mrgI(),mrgD());
        cy1=cy()+escala*(float)map(x1[i],minimo,maximo,_alto/(-2),_alto/(2));        
        cy1=cy1<=mrgS()?mrgS():cy1;
        vertex(cx1,  cy1>=mrgF()?mrgF():cy1);
      }
      endShape();
    }

    public void pintaVector(double[] x1,float escala) {
      float cx1,cy1;
      dibuja_fondo();
      stroke(250, 0, 0);     //red                          
      noFill();
      beginShape();             
      for (int i = 0; i < x1.length; i++) {
        cx1=(float)map(i,0,x1.length,mrgI(),mrgD());
        cy1=cy()+(float)escala*(float)x1[i];
        vertex(cx1, cy1>mrgF()?mrgF():cy1);
      }
      endShape();
    }

}
  public void settings() {  size(600, 400); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "MBvisorpc2" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

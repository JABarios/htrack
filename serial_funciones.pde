
void serDecode_simulando() { //assuming Arduino is only transfering active channels

 int num_puntos=4; //variable empirica, para aproximar lo mostrado a lo q sale del BT
 if (calibrationFrames == 0) { //drawing display
  for(int ii=0;ii<num_puntos;ii++){
    for (int ch = 0; ch < gOscChannels; ch++) 
                  values[ch][cnt] = (int)(90*sin(2*3.1415*gFrecuenciaMuestra/900*cnt2))+(int)(3*90*pow(sin(2*3.1415*gFrecuenciaMuestra2/900*cnt2),1));  //put it in the array
                 
     cnt++;  //increment the count
     cnt2++;
     if (cnt > wm1) cnt = 1;
  }
 } else {
            valuesReceived+=num_puntos;
 }
 
}               

void serDecode_simple() { //assuming Arduino is only transfering active channels
 parse_linea_leida();
}               

void parse_linea_leida() {
  //trocea el global linea_leida
  if (linea_leida.length()<9 || linea_leida.lastIndexOf(';')<0)return; // si linea  muy corta o aún incompleta, volvemos hasta que el BT la rellene
  String lines[] = linea_leida.split(";");
  String numeros[] ;
  for (int i=0; i<lines.length-1; i++) {
    numeros = lines[i].split(",");
    // el primer valor de la linea no se usa, se lo traga el BT no sé por qué (bug)
    // tal vez sea el \r|\n o algo así?
    int n1=int(numeros[1]);
    int n2=int(numeros[2]);
     println(nf(n1,3)+"  "+nf(n2,12));
    values[0][cnt] = n1;
    values[1][cnt] = n1;
     cnt++;  //increment the count
     cnt2++;
     if (cnt > wm1) cnt = 1;
  }
  linea_leida="";
}
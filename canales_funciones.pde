void canales_inicia(){
 startTime = 0;
  size(screenWid, screenHt);                                  //currently set to 5 sec
  //serialBytes = new int[packetBytes];
  val = new int[gOscChannels]; //most recent sample for each channel
  values = new int[gOscChannels][width]; //previous samples for each channel
  wm1= width-1; 
  cnt = 1;     
  for (int c=0;c<gOscChannels;c++) {
    for (int s=0;s<width;s++) {                 //set initial values to midrange
      values[c][s] = 0;
    }//for each sample
  }//for each channel 
 
}

void autoScaleChannel (int ch) {
  int Ht = screenHt-Margin-Margin;
  if (offsetTraces) Ht = Ht - (gOscChannels * 2);
  if ((Ht < 1) || (ch < 0) || (ch >= kOscMaxChannels) || (channelMin[ch] > channelMax[ch])) return;
  if (channelMin[ch] == channelMax[ch]) {
    channelSlope[ch] = 0;
    return; 
  }
  channelSlope[ch] = float(Ht)/ float(channelMax[ch] - channelMin[ch]);
} //autoScaleChannel()
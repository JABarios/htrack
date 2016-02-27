void inicia_bluetooth() {
//  bt = new KetaiBluetooth(this);
  ArrayList names;
  background(78, 93, 75);
  klist = new KetaiList(this, bt.getPairedDeviceNames());
  bt.start();
  isConfiguring = false;
}

// Call back method to manage data received
// lee los datos que llegan por BT. Les añade lo que quedó de la llamada anterior (resto_linea, global),
// y pasa al buffer global linea_leida todo hasta el ultimo punto y coma. El resto se almacena en
// resto_linea, hasta la siguiente llegada de datos BT
void onBluetoothDataEvent(String who, byte[] data) {
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

void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  bt = new KetaiBluetooth(this);
}

void onKetaiListSelection(KetaiList klist) {
  String selection = klist.getSelection();
  bt.connectToDeviceByName(selection);
  //dispose of list for now
  klist = null;
}
// supongo que esto lo necesita la libreria ketai
void onActivityResult(int requestCode, int resultCode, Intent data) {
  bt.onActivityResult(requestCode, resultCode, data);
}

String getBluetoothInformation()
{
  BT_id="";
  String btInfo = "Server Running: ";
  btInfo += bt.isStarted() + "\n";
  btInfo += "Device Discoverable: "+bt.isDiscoverable() + "\n";
  btInfo += "\nConnected Devices: \n";
  ArrayList<String> devices = bt.getConnectedDeviceNames();
  for (String device: devices)
  {
  BT_id+= device+"\n";
  btInfo+= device+"\n";
  }
  m_print(btInfo);
  return btInfo;
}
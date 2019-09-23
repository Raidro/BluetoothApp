package com.unp.bluetapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //definindo o que vamos precisa

    BluetoothDevice bluetoothDevice = null;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null; // e uma boa pratica inicar como nulos
    public UUID myUUID;


    /* definindo os objetos */

    Button btnPareado, btnConectar, btnEnviar;
    Switch swBT;
    ImageView imgBT;
    ListView lstBT;
    EditText edtEnviar;
    public ArrayAdapter<String> lstAdapter;
    public ArrayList<String> dadosBT = new ArrayList<>(); // tem que iniializar o botao;
    String[] end_mac;
    ConnectThread connectThread;
    private  Handler myHandler;


    /* ========================= */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        btnConectar = findViewById(R.id.btnConectar);
        btnPareado = findViewById(R.id.btnPareado);
        swBT = findViewById(R.id.swBT);
        imgBT = findViewById(R.id.imgBT);
        lstBT = findViewById(R.id.lstBT);
        btnEnviar = findViewById(R.id.btnEnviar);
        edtEnviar = findViewById(R.id.edtEnviar);


        //criar a instancia do adptador bluethoot

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //verificar se o bluethoot esta acessivel

        if (bluetoothAdapter == null) {
            msgToast("Bluethoot nao esta acessivel!");
        } else {
            msgToast("Bluethoot acessivel!");
        }

        //se o bluethoot esta ativo

        try {
            if (bluetoothAdapter.isEnabled()) {
                msgToast("Bluethoot esta Ativo!");
                imgBT.setImageResource(R.drawable.ic_blue_on);
                swBT.setChecked(true);
                btnPareado.setEnabled(true);

            } else {
                msgToast("Bluethoot esta Inativo!");
                imgBT.setImageResource(R.drawable.ic_blue_off);
                swBT.setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //criando o evento do switch
        swBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (swBT.isChecked()) {

                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 0);//codigo de request
                    swBT.setText("Desativar BT");
                } else if (!swBT.isChecked()) {
                    if (bluetoothAdapter.isEnabled()) {

                        bluetoothAdapter.disable();
                        msgToast("Bluethoot desativado!");
                        imgBT.setImageResource(R.drawable.ic_blue_off);
                        btnPareado.setEnabled(false);
                        btnConectar.setEnabled(false);
                        swBT.setText("Ativar BT");
                        btnEnviar.setEnabled(false);
                        edtEnviar.setEnabled(false);
                        //limpar a lista e desabilitar os botoes
                        if (!dadosBT.isEmpty()) {
                            dadosBT.clear();
                            lstAdapter.clear();

                        }

                    }
                }

            }
        }); // final do evento do switch

        btnPareado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!bluetoothAdapter.isDiscovering()) {

                    Intent intent2 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent2, 1);

                }

            }
        });// final do evento do botao parear

        //evento de clicar na lista de dispositivos

        lstBT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                end_mac = lstAdapter.getItem(position).split(",");
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(end_mac[1].trim());
                msgToast("Endereco MAC: " + end_mac[1].trim());
                btnConectar.setEnabled(true);

            }
        }); // final do evento de clicar na lista de dispositivos


        //evento de se concetar ao para conectar ao BT(device)

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (swBT.isChecked() && btnConectar.getText().equals("CONECTAR")) {


                    try {

                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(myUUID);
                        bluetoothSocket.connect();
                        imgBT.setImageResource(R.drawable.ic_blue_conn);
                        msgToast("Conectado com dispositivo: " + end_mac[1].trim());
                        btnConectar.setText("DESCONECTAR");
                        btnEnviar.setEnabled(true);
                        edtEnviar.setEnabled(true);


                        //chamar a thread de conexao
                        connectThread = new ConnectThread(bluetoothSocket, myHandler);
                        connectThread.start();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try { // muda o botao de conectar para desconectar
                        bluetoothSocket.close();
                        btnConectar.setText("CONECTAR");
                        imgBT.setImageResource(R.drawable.ic_blue_on);
                        msgToast("Desconectado do disposivito bluothoot!");
                        btnEnviar.setEnabled(false);
                        edtEnviar.setEnabled(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });//fnal do evento de conectar

        //evento do botao de enviar dados para o dispositivo blouthoot

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txtDado = edtEnviar.getText().toString();

                if (!txtDado.isEmpty()) {
                    connectThread.enviaDados(txtDado); // envia a informacao para o bluotooth
                } else {
                    msgToast(" Digite Dados a enviar para o BT...!");
                }

            }
        });

        //Handle para comunicacao com a thread

        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0) {
                    msgToast("Dados Recebidos: " + msg.obj.toString());
                }

            }
        };


        //final do evento de enviar do botao


    } // final do on create

    private void msgToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }//final do msgToast

    protected void onActivityResult(int resquestCode, int resultCode, @Nullable Intent data) {
        switch (resquestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    msgToast("Bluethoot Ativo!");
                    imgBT.setImageResource(R.drawable.ic_blue_on);
                    btnPareado.setEnabled(true);

                } else {
                    msgToast("Bluethoot nao ativado!");
                    imgBT.setImageResource(R.drawable.ic_blue_off);
                }

                break;
            case 1:
                if (resultCode == 120) { // 120 e o codigo igual ao tempo de discovery
                    dadosBT.clear();
                    dadosBT = new ArrayList<>();
                    if (bluetoothAdapter.isEnabled()) {
                        Set<BluetoothDevice> BTdevices = bluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice device : BTdevices) {

                            dadosBT.add(device.getName() + ", " + device.getAddress());

                        }
                        lstAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, dadosBT);
                        lstBT.setAdapter(lstAdapter);
                        if (!dadosBT.isEmpty()) {
                            btnConectar.setEnabled(false);
                        } else {
                            msgToast("Nenhum dispositivo encontrado!");
                        }
                    } else {
                        msgToast("Bluethoot nao esta ativo!");
                    }
                }

        }

        super.onActivityResult(resquestCode, resultCode, data);
    }


}

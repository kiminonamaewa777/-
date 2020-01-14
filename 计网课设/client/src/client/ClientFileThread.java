// ClientFileThread.java
package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class ClientFileThread extends Thread{
    private Socket socket = null;
    private JFrame chatViewJFrame = null;
    static String userName = null;
    static PrintWriter out = null;  // ��ͨ��Ϣ�ķ��ͣ�Server.java������ֵ��
    static DataInputStream fileIn = null;
    static DataOutputStream fileOut = null;
    static DataInputStream fileReader = null;
    static DataOutputStream fileWriter = null;
    static String hostIp=null;
    static FileSystemView fsv=FileSystemView.getFileSystemView();
    static File com=fsv.getHomeDirectory();
    static JFrame frame=null;

    public ClientFileThread(String userName, String hostIp,JFrame chatViewJFrame, PrintWriter out,JFrame frame) {
        ClientFileThread.userName = userName;
        ClientFileThread.hostIp=hostIp;
        this.chatViewJFrame = chatViewJFrame;
        ClientFileThread.out = out;
        ClientFileThread.frame=frame;
    }

    // �ͻ��˽����ļ�
    public void run() {
        try {
            //InetAddress addr = InetAddress.getByName(null);  // ��ȡ������ַ
            socket = new Socket(hostIp, 8090);  // �ͻ����׽���
            fileIn = new DataInputStream(socket.getInputStream());  // ������
            fileOut = new DataOutputStream(socket.getOutputStream());  // �����
            // �����ļ�
            while(true) {
                String textName = fileIn.readUTF();
                long totleLength = fileIn.readLong();
                int result = JOptionPane.showConfirmDialog(chatViewJFrame, "�Ƿ���ܣ�", "��ʾ",
                        JOptionPane.YES_NO_OPTION);
                int length = -1;
                byte[] buff = new byte[1024];
                long curLength = 0;
                // ��ʾ��ѡ������0Ϊȷ����1λȡ��
                if(result == 0){
//					out.println("��" + userName + "ѡ���˽����ļ�����");
//					out.flush();
                    File topFile = new File(com+"\\�����ļ�");
                    if(!topFile.exists()) {  // �½������ļ���
                        topFile.mkdir();
                    }
                    File userFile = new File(com+"\\�����ļ�\\" + userName);
                    if(!userFile.exists()) {  // �½���ǰ�û����ļ���
                        userFile.mkdir();
                    }
                    File file = new File(com+"\\�����ļ�\\" + userName + "\\"+ textName);
                    fileWriter = new DataOutputStream(new FileOutputStream(file));
                    String selectUser = "������";
                    // ��ʾ�ļ���ŵ�ַ
                    JOptionPane.showMessageDialog(chatViewJFrame, "�ļ���ŵ�ַ��\n" +
                            com+"\\�����ļ�\\" +
                            userName + "\\" + textName, "��ʾ", JOptionPane.INFORMATION_MESSAGE);
                    while((length = fileIn.read(buff)) > 0) {  // ���ļ�д������
                        fileWriter.write(buff, 0, length);
                        fileWriter.flush();
                        curLength += length;
//						out.println("�����ս���:" + curLength/totleLength*100 + "%��");
//						out.flush();
                        if(curLength == totleLength) {  // ǿ�ƽ���
                            out.println("TOALL@" + frame.getTitle() + "@" + selectUser + "@" +"��" + userName + "�������ļ�����");
                            out.flush();
                            break;
                        }
                    }
                }
                else {  // �������ļ�
                    while((length = fileIn.read(buff)) > 0) {
                        curLength += length;
                        if(curLength == totleLength) {  // ǿ�ƽ���
                            break;
                        }
                    }
                }
                fileWriter.close();
            }
        } catch (Exception e) {}
    }

    // �ͻ��˷����ļ�
    static void outFileToServer(String path) {
        try {
            File file = new File(path);
            fileReader = new DataInputStream(new FileInputStream(file));
            fileOut.writeUTF(file.getName());  // �����ļ�����
            fileOut.flush();
            fileOut.writeLong(file.length());  // �����ļ�����
            fileOut.flush();
            int length = -1;
            byte[] buff = new byte[1024];
            while ((length = fileReader.read(buff)) > 0) {  // ��������

                fileOut.write(buff, 0, length);
                fileOut.flush();
            }

            String selectUser = "������";
            out.println("TOALL@" + frame.getTitle() + "@" + selectUser + "@" +"��" + userName + "�ѳɹ������ļ�����");
            out.flush();
        } catch (Exception e) {}
    }
}


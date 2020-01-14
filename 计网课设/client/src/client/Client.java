package client;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import server.User;

public class Client {

    private JFrame frame;
    private JTextArea textArea;

    private JTextField textField;

    private JTextField txt_port;
    private JTextField txt_hostIp;
    private JTextField txt_name;

    private JButton start;
    private JButton stop;
    private JButton send;
    private JButton sendFile;

    private JRadioButton groupchat;//Ⱥ�İ�ť
    private JRadioButton privatechat;//˽�İ�ť
    private ButtonGroup buttongroup;//��ť��
    private JPanel buttonPanel;
    private JPanel northPanel;
    private JPanel southPanel;

    private JScrollPane rightScroll;
    private JScrollPane leftScroll;//��������
    private JSplitPane centerSplit;


    private JList<String> userList;
    private DefaultListModel<String> listModel;

    private boolean isConnected = false;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;// ���������Ϣ���߳�
    private Map<String, User> onLineUsers = new HashMap<String, User>();// ���������û������Ժ�����˽��

    static FileSystemView fsv=FileSystemView.getFileSystemView();
    static File com=fsv.getHomeDirectory();

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        textArea = new JTextArea();
        textArea.setEditable(false);

        textField = new JTextField();
        txt_port = new JTextField("12345");
        txt_hostIp = new JTextField("192.168.123.68");
        Random rand = new Random();


        txt_name = new JTextField("�û�" + rand.nextInt(100));
        start = new JButton("����");
        stop = new JButton("�Ͽ�");
        send = new JButton("����");
        sendFile = new JButton("�����ļ�");
        listModel = new DefaultListModel<String>();
        userList = new JList<String>(listModel);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 7));
        northPanel.add(new JLabel("�˿�"));
        northPanel.add(txt_port);
        northPanel.add(new JLabel("������IP"));
        northPanel.add(txt_hostIp);
        northPanel.add(new JLabel("�ǳ�"));
        northPanel.add(txt_name);
        northPanel.add(start);
        northPanel.add(stop);
        northPanel.setBorder(new TitledBorder("������Ϣ"));

        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(new TitledBorder("��Ϣ��ʾ��"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder("�����û�"));

        groupchat = new JRadioButton("Ⱥ��");
        privatechat = new JRadioButton("˽��");

        privatechat.setSelected(true);
        buttongroup = new ButtonGroup();
        buttongroup.add(groupchat);
        buttongroup.add(privatechat);
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(groupchat);
        buttonPanel.add(privatechat);

        southPanel = new JPanel(new BorderLayout());
        JPanel inSouthPanel= new JPanel(new BorderLayout());
        inSouthPanel.add(send, "West");
        inSouthPanel.add(sendFile, "East");
        southPanel.add(buttonPanel, "North");
        southPanel.add(textField, "Center");
        southPanel.add(inSouthPanel,"East");
        southPanel.setBorder(new TitledBorder("д��Ϣ"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(100);

        frame = new JFrame("�ͻ���");

        frame.setLayout(new BorderLayout());

        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(600, 400);
        frame.setVisible(true);

        // �������Ͱ�ť�¼�
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        //���������ļ���ť�¼�
        sendFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });

        // �������Ӱ�ť�¼�
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int port;
                if (isConnected) {
                    JOptionPane.showMessageDialog(frame, "�Ѵ���������״̬����Ҫ�ظ�����!",
                            "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    try {
                        port = Integer.parseInt(txt_port.getText().trim());
                    } catch (NumberFormatException e2) {
                        throw new Exception("�˿ںŲ�����Ҫ��!�˿�Ϊ����!");
                    }
                    String hostIp = txt_hostIp.getText().trim();
                    String name = txt_name.getText().trim();
                    if (name.equals("") || hostIp.equals("")) {
                        throw new Exception("�ǳơ�������IP����Ϊ��!");
                    }
                    boolean flag = connectServer(port, hostIp, name);
                    if (flag == false) {
                        throw new Exception("�����������ʧ��!");
                    }
                    frame.setTitle(name);
                    JOptionPane.showMessageDialog(frame, "�ɹ�����!");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "����", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // �����Ͽ���ťʱ�¼�
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isConnected) {
                    JOptionPane.showMessageDialog(frame, "�Ѵ��ڶϿ�״̬����Ҫ�ظ��Ͽ�!",
                            "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    boolean flag = closeConnection();// �Ͽ�����
                    if (flag == false) {
                        throw new Exception("�Ͽ����ӷ����쳣��");
                    }
                    JOptionPane.showMessageDialog(frame, "�ɹ��Ͽ�!");
                    listModel.removeAllElements();
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "����", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // �رմ���ʱ�¼�
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isConnected) {
                    closeConnection();// �ر�����
                }
                System.exit(0);// �˳�����
            }
        });


    }

    // ִ�з���
    public void send() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "��û�����ӷ��������޷�������Ϣ��", "����",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = textField.getText().trim();
        String selectUser = "������";

        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "��Ϣ����Ϊ�գ�", "����",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (groupchat.isSelected()) {

            sendMessage("TOALL@" + frame.getTitle() + "@" + selectUser + "@" + message);

            //	System.out.println("Ⱥ�ķ���");
            textField.setText("");
        }
        if (privatechat.isSelected()) {

            selectUser = (String) userList.getSelectedValue();
            if (selectUser == null) {
                JOptionPane.showMessageDialog(frame, "��ѡ����˽�ĵ��û�!");
                return;
            }
            sendMessage("TOONE@" + frame.getTitle() + "@" + selectUser + "@" + message);
            String t = "��@" + selectUser + "˵:" + message + "\r\n";

            textArea.append(t);
            //textArea.setForeground(Color.BLUE);
            textField.setText("");


        }

    }

    // ִ�з���
    public void sendFile() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "��û�����ӷ��������޷�������Ϣ��", "����",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // ����һ��Ĭ�ϵ��ļ�ѡ����
        JFileChooser fileChooser = new JFileChooser();
        // ����Ĭ����ʾ���ļ���
        fileChooser.setCurrentDirectory(new File(String.valueOf(com)));
        // ��ӿ��õ��ļ���������FileNameExtensionFilter �ĵ�һ������������, ��������Ҫ���˵��ļ���չ����
//        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("(txt)", "txt"));
        // ����Ĭ��ʹ�õ��ļ���������FileNameExtensionFilter �ĵ�һ������������, ��������Ҫ���˵��ļ���չ�� �ɱ������
        fileChooser.setFileFilter(new FileNameExtensionFilter("(txt)", "txt"));
        // ���ļ�ѡ����߳̽���������֪��ѡ��򱻹رգ�
        int result = fileChooser.showOpenDialog(frame);  // �Ի��򽫻ᾡ����ʾ�ڿ��� parent ������
        // ���ȷ��
        if(result == JFileChooser.APPROVE_OPTION) {
            // ��ȡ·��
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            ClientFileThread.outFileToServer(path);
        }

    }

    //���ӷ�����
    public boolean connectServer(int port, String hostIp, String name) {
        // ���ӷ�����
        try {
            socket = new Socket(hostIp, port);// ���ݶ˿ںźͷ�����ip��������
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // ���Ϳͻ����û�������Ϣ(�û�����ip��ַ)
            sendMessage(name + "@" + socket.getLocalAddress().toString());
            // ����������Ϣ���߳�
            messageThread = new MessageThread(reader, textArea);
            messageThread.start();
            isConnected = true;// �Ѿ���������

            ClientFileThread fileThread = new ClientFileThread(name,hostIp,frame, writer,frame);
            fileThread.start();

            return true;

        } catch (Exception e) {
            textArea.append("��˿ں�Ϊ��" + port + "    IP��ַΪ��" + hostIp
                    + "   �ķ���������ʧ��!" + "\r\n");
            isConnected = false;// δ������
            return false;
        }
    }


    //������Ϣ
    public void sendMessage(String message) {

        writer.println(message);
        writer.flush();

    }

    //�ͻ��������ر�
    @SuppressWarnings("deprecation")
    public synchronized boolean closeConnection() {
        try {
            sendMessage("CLOSE");// ���ͶϿ����������������
            messageThread.stop();// ֹͣ������Ϣ�߳�
            // �ͷ���Դ
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isConnected = true;
            return false;
        }
    }


    // ���Ͻ�����Ϣ���߳�
    class MessageThread extends Thread {
        private BufferedReader reader;
        private JTextArea textArea;
        String username = textField.getName();

        // ������Ϣ�̵߳Ĺ��췽��
        public MessageThread(BufferedReader reader, JTextArea textArea) {
            this.reader = reader;
            this.textArea = textArea;
        }

        // �����Ĺر�����
        public synchronized void closeCon() throws Exception {
            // ����û��б�
            listModel.removeAllElements();
            // �����Ĺر������ͷ���Դ
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isConnected = false;// �޸�״̬Ϊ�Ͽ�
        }

        public void run() {
            String message = null;
            while (true) {
                try {
                    message = reader.readLine();
                    StringTokenizer st = new StringTokenizer(message, "/@");

                    String parts = st.nextToken();//����
                    switch (parts) {
                        case "CLOSE": {
                            textArea.append("�������ѹر�!\r\n");
                            closeCon();// �����Ĺر�����
                            return;// �����߳�
                        }
                        case "USERLIST": {


                            int size = Integer.parseInt(st.nextToken());
                            String username = null;
                            String userIp = null;

                            for (int i = 0; i < size; i++) {
                                username = st.nextToken();
                                userIp = st.nextToken();
                                User user = new User(username, userIp);
                                onLineUsers.put(username, user);
                                listModel.addElement(username);
                            }
                            break;
                        }
                        case "ADD": {
                            String username = "";
                            String userIp = "";
                            if ((username = st.nextToken()) != null
                                    && (userIp = st.nextToken()) != null) {

                                User user = new User(username, userIp);
                                onLineUsers.put(username, user);
                                listModel.addElement(username);
                                textArea.append("ϵͳ��ʾ��" + username + "����!\r\n");
                            }

                            break;
                        }
                        case "DELETE": {
                            String username = st.nextToken();
                            //	System.out.println(username+"����");
                            User user = (User) onLineUsers.get(username);
                            onLineUsers.remove(user);
                            listModel.removeElement(username);
                            textArea.append("ϵͳ��ʾ:" + username + "����!\r\n");
                            userList.setModel(listModel);
                            break;
                        }
                        case "TOALL": {
                            textArea.append(st.nextToken() + "\r\n");
                            //System.out.println("Ⱥ��");
                            break;
                        }
                        case "TOONE": {
                            textArea.append(st.nextToken() + "\r\n");
                            //System.out.println("˽��");
                            //textArea.setForeground(Color.BLUE);//˽�ĵ���ϢΪ��ɫ
                            break;
                        }
                        default:
                            textArea.append(message + "\r\n");
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

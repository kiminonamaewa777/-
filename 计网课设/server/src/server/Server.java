package server;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class Server extends JFrame {

    private static final long serialVersionUID = 1L;
    private ServerSocket serveSocket;
    private ServerThread serverThread;
    private ArrayList<ClientThread> clients;

    private JFrame frame;
    private JTextArea txt1;
    private JTextField txt_message;
    private JTextField txt_port;
    private JButton start;
    private JButton send;
    private JButton stop;
    private JPanel northPanle;
    private JPanel southPanle;
    private JScrollPane leftPanle;
    private JScrollPane rightPanle;
    private JSplitPane centerSplit;

    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private boolean isStart = false;
    private ServerFileThread serverFileThread;
    public static void main(String[] args) {
        new Server();

    }

    //���캯��
    public Server() {
        frame = new JFrame("������");
        txt1 = new JTextArea();
        txt_message = new JTextField(30);
        txt_port = new JTextField("12345");
        start = new JButton("�����˶˿�");
        stop = new JButton("ֹͣ������");
        send = new JButton("����");
        listModel = new DefaultListModel<String>();
        userList = new JList<String>(listModel);
        //userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        southPanle = new JPanel(new BorderLayout());
        southPanle.setBorder(new TitledBorder("д��Ϣ"));
        southPanle.add(txt_message, BorderLayout.CENTER);
        southPanle.add(send, BorderLayout.EAST);

        leftPanle = new JScrollPane(userList);
        leftPanle.setBorder(new TitledBorder("�����û�"));

        rightPanle = new JScrollPane(txt1);
        rightPanle.setBorder(new TitledBorder("��Ϣ��ʾ��"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanle, rightPanle);
        centerSplit.setDividerLocation(100);

        northPanle = new JPanel();
        northPanle.setLayout(new GridLayout(1, 6));
        northPanle.add(start);
        northPanle.add(txt_port);
        northPanle.add(stop);

        frame.setLayout(new BorderLayout());
        frame.add(northPanle, BorderLayout.NORTH);
        frame.add(centerSplit, BorderLayout.CENTER);
        frame.add(southPanle, BorderLayout.SOUTH);
        frame.setSize(600, 400);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @SuppressWarnings("unused")
            public void windowclosing(WindowEvent e) {
                if (isStart) {
                    closeServer();//�رշ�����
                }
                System.exit(0);//�˳�����
            }
        });
        @SuppressWarnings("unused")
        int port = Integer.parseInt(txt_port.getText());
//	�����˿��¼���������������
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isStart) {
                    JOptionPane.showMessageDialog(frame, "�������Ѵ�������״̬", "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int port;
                try {
                    try {
                        port = Integer.parseInt(txt_port.getText());
                    } catch (Exception e1) {
                        throw new Exception("�˿ں� Ϊ��������");
                    }
                    if (port <= 0) {
                        throw new Exception("�˿ں� Ϊ��������");

                    }

                    serverFileThread = new ServerFileThread();//�����ļ��߳�
                    serverFileThread.start();

                    serverStart(port);
                    txt1.append("������������,�˿�:" + port + ",���ڵȴ��ͻ�������...\r\n");
                    JOptionPane.showMessageDialog(frame, "�������ɹ�����");
                    start.setEnabled(false);
                    txt_port.setEnabled(false);
                    stop.setEnabled(true);
                } catch (Exception ee) {
                    JOptionPane.showMessageDialog(frame, ee.getMessage(), "����", JOptionPane.ERROR_MESSAGE);
                }

            }

        });
        // ����ֹͣ��������ťʱ�¼�
        stop.addActionListener(new ActionListener() {
            @SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
                if (!isStart) {
                    JOptionPane.showMessageDialog(frame, "��������δ������", "����", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    closeServer();
                    serverFileThread.stop();
                    start.setEnabled(true);
                    txt_port.setEnabled(true);
                    stop.setEnabled(false);
                    txt1.append("�������ɹ�ֹͣ!\r\n");
                    JOptionPane.showMessageDialog(frame, "�������ɹ�ֹͣ��");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, "ֹͣ�����������쳣��", "����",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        // �������Ͱ�ťʱ�¼�
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

    }


    //����������
    public void serverStart(int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serveSocket = new ServerSocket(port);
            serverThread = new ServerThread(serveSocket);
            serverThread.start();
            isStart = true;
        } catch (BindException e) {
            isStart = false;
            throw new BindException("�˿ں��ѱ�ռ�ã���һ��");
        } catch (Exception e) {
            e.printStackTrace();
            isStart = false;
            throw new BindException("�����������쳣");
        }

    }

    // �رշ�����
    @SuppressWarnings("deprecation")
    public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();// ֹͣ�������߳�

            if(serverFileThread!=null) {
                serverFileThread.closeThread();
                serverFileThread.stop();
            }

            for (int i = clients.size() - 1; i >= 0; i--) {
                // �����������û����͹ر�����
                clients.get(i).getWriter().println("CLOSE");
                clients.get(i).getWriter().flush();
                // �ͷ���Դ
                clients.get(i).stop();// ֹͣ����Ϊ�ͻ��˷�����߳�
                clients.get(i).reader.close();
                clients.get(i).writer.close();
                clients.get(i).socket.close();
                clients.remove(i);
            }
            if (serveSocket != null) {
                serveSocket.close();// �رշ�����������
            }
            listModel.removeAllElements();// ����û��б�
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
            isStart = true;
        }
    }

    // ִ����Ϣ����
    public void send() {
        if (!isStart) {
            JOptionPane.showMessageDialog(frame, "������δ���������ܷ�����Ϣ��", "����",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clients.size() == 0) {
            JOptionPane.showMessageDialog(frame, "û���û�����,���ܷ�����Ϣ��", "����",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = txt_message.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "��Ϣ����Ϊ�գ�", "����",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendServerMessage(message);// Ⱥ����������Ϣ
        txt1.append("��������ʾ��" + txt_message.getText() + "\r\n");//������˵�Ļ���ʾ�ڷ���������
        txt_message.setText(null);
    }

    // �Ѻ�̨��Ϣ���͸������ͻ���
    public void sendServerMessage(String message) {
        for (int i = clients.size() - 1; i >= 0; i--) {
            clients.get(i).getWriter().println("ϵͳ��ʾ��" + message + "(Ⱥ��)");//��������õ���������͸��ͻ��˽���
            clients.get(i).getWriter().flush();
        }
    }


    //ÿ�����ӵ��������Ŀͻ��ˣ�������֮��Ӧ��һ���߳������������շ���Ϣ
    class ClientThread extends Thread {

        Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        private User user;

        public BufferedReader getReader() {
            return reader;

        }

        public PrintWriter getWriter() {
            return writer;

        }

        public User getUser() {
            return user;

        }

        //ÿ���ͻ��˶�Ӧһ���ͻ����̴߳���
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                //���տͻ��˵Ļ�����Ϣ
                String line = reader.readLine();
                String[] st = line.split("@");

                user = new User(st[0], st[1]);
                //�������ӳɹ���Ϣ
                writer.println("ϵͳ��ʾ��" + user.getName() + user.getIp() + "����������ӳɹ�!");

                //System.out.println(user.getName()+".."+user.getIp());
                //������ǰ�û���Ϣ
                if (clients.size() > 0) {
                    String temp = "";
                    for (int i = clients.size() - 1; i >= 0; i--) {
                        temp += (clients.get(i).getUser().getName() + "/" + clients.get(i).getUser().getIp()) + "@";

                    }

                    writer.println("USERLIST@" + clients.size() + "@" + temp);
                    writer.flush();

                }
                System.out.println(st[0] + ",��������ʾ����" + st[1]);
                //�����������û����͸��û���������,���������ߵ��û�����������û��б���
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println("ADD@" + user.getName() + "@" + user.getIp());
                    clients.get(i).getWriter().flush();
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }

        @SuppressWarnings("deprecation")
        public void run() {//���Ͻ��ܿͻ��˵���Ϣ���д���
            String message = null;
            while (true) {
                try {
                    message = reader.readLine();//���տͻ�����Ϣ
                    System.out.println(message);
                    if (message.equals("CLOSE"))// ��������
                    {
                        txt1.append(this.getUser().getName() + this.getUser().getIp() + "����!\r\n");

                        // �Ͽ������ͷ���Դ
                        reader.close();
                        writer.close();
                        socket.close();

                        // �����������û����͸��û�����������
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            clients.get(i).getWriter().println("DELETE@" + user.getName());
                            clients.get(i).getWriter().flush();
                        }

                        listModel.removeElement(user.getName());// ���������б�
                        // ɾ�������ͻ��˷����߳�
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            if (clients.get(i).getUser() == user) {
                                ClientThread temp = clients.get(i);
                                clients.remove(i);// ɾ�����û��ķ����߳�
                                temp.stop();// ֹͣ���������߳�
                                return;
                            }
                        }

                    } else {
                        dispatcherMessage(message);// ת����Ϣ
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }

        private void dispatcherMessage(String message) {


            String[] parts = message.split("@");
            String string = parts[1] + "��" + parts[2] + "˵:" + parts[3];
            if (parts[0].equals("TOALL")) {// Ⱥ��
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println("TOALL@" + string);
                    clients.get(i).getWriter().flush();
                    //System.out.println("Ⱥ����Ϣ����");
                }
                txt1.append(string + "\r\n");
            }
            if (parts[0].equals("TOONE")) {//˽��
                for (int i = 0; i < clients.size(); i++) {
                    if (parts[2].equals((clients.get(i).getUser().getName()))) {
                        string = parts[1] + "����˵:" + parts[3];
                        clients.get(i).getWriter().println("TOONE@" + string);
                        clients.get(i).getWriter().flush();
                        //System.out.println("˽����Ϣ����");
                    }
                }
                txt1.append(parts[1] + "��" + parts[2] + "˵:" + parts[3] + "\r\n");
            }
        }

    }

    class ServerThread extends Thread {
        private ServerSocket serverSocket;

        // �������̵߳Ĺ��췽��
        public ServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;

        }

        public void run() {
            while (true) {// ��ͣ�ĵȴ��ͻ��˵�����
                try {
                    Socket socket = serverSocket.accept();
                    ClientThread client = new ClientThread(socket);
                    client.start();// �����Դ˿ͻ��˷�����߳�
                    clients.add(client);
                    listModel.addElement(client.getUser().getName());// ���������б�
                    txt1.append(client.getUser().getName() + client.getUser().getIp() + "����!\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
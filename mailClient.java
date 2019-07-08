import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Scanner;
import static java.lang.System.exit;

public class mailClient {
    private int imapPort=993; // ssl/tls 지원 imap port number
    private int smtpPort=465; // ssl/tls 지원 smtp port number
    private String imapServer;
    private String smtpServer;
    private String id;
    private String pw;
    private String mailserver;


    public void makeimaphost(String ident)  //imap host 만들기
    {
        int index = ident.indexOf("@");
        mailserver = ident.substring(index+1);   //@뒤에 있는 스트링저장

        imapServer = "imap."+mailserver;// 결과 예시 : imap.google.com
    }
    public void makesmtphost(String ident)
    {
        int index = ident.indexOf("@");
        mailserver = ident.substring(index+1);   //@뒤에 있는 스트링저장

        smtpServer = "smtp."+mailserver;// 결과 예시 : smtp.gmail.com
    }
    public boolean loginImap(OutputStream outToServer, BufferedReader inFromServer) throws Exception    //로그인하는 함수
    {
        String isConnect;   // 서버로부터의 응답을 받을 string
        boolean isSuccess = false;  // 결과가 성공적인지 저장할 boolean

        String login = "foo LOGIN "+id+" "+pw+"\r\n";
        outToServer.write(login.getBytes());
        /*---------google인지 아닌지 확인---------*/
        if (imapServer.equals("imap.gmail.com")) { // google은 서버의 응답이 2번 옴
            isConnect = inFromServer.readLine();
            if (!isConnect.contains("AUTHENTICATIONFAILED")) { // 비밀번호 틀릴 시, 응답 1번만 옴
                isConnect = inFromServer.readLine();
                System.out.println(isConnect);
            }
        }
        else { // google을 제외한 나머지는 응답 1번
            isConnect = inFromServer.readLine();
        }

        /*---------로그인 성공여부 확인---------*/
        if (isConnect.startsWith("foo OK")){ // 로그인 성공
            System.out.println("Login completed");
            System.out.println();
            isSuccess = true;
        }
        else {
            System.out.println("Login failure");
            isSuccess = false;
        }

        return isSuccess;
    }

    public boolean connectImap(BufferedReader inFromServer) throws Exception    //imap 연결 함수
    {
        String isConnect = inFromServer.readLine();
        boolean isSuccess = false;

        if(isConnect.startsWith("* OK")){ // connection이 잘 되었을 때
            System.out.println();
            System.out.println("IMAP Server connection Accept");
            isSuccess = true;
        }
        else {   // connection이 안되었을때
            System.out.println("IMAP Server connection Failed");
            isSuccess = false;
        }

        return isSuccess;
    }

    public void createFolder() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        // imap.google.com에 연결하는 socket형성
        Socket clientSocket = new Socket(imapServer, imapPort);

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(clientSocket,clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect;
        if (!connectImap(inFromServer)) { // Imap 서버 연결 실패
            clientSocket.close();
            exit(1);
        }

        /*------ LOGIN 수행 -------*/
        if (!loginImap(outToServer, inFromServer)) { // 로그인 실패
            clientSocket.close();
            exit(1);
        }

        /*------ CREATE 수행 -------*/
        System.out.println("## Folder Creation ##");
        System.out.print("New Folder Name : ");
        String folderName = in.next();//폴더 이름 입력 받음

        String create = "foo CREATE "+folderName+"\r\n";
        outToServer.write(create.getBytes());

        isConnect = inFromServer.readLine();
        if (isConnect.startsWith("foo OK")){
            System.out.println("Folder Created");
            System.out.println();
        }
        else {
            System.out.println(isConnect);
            System.out.println("Failed to Create Folder");
            clientSocket.close();
            exit(1);
        }

        clientSocket.close();
    }
    public void deleteFolder() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        // imap.google.com에 연결하는 socket형성
        Socket clientSocket = new Socket(imapServer, imapPort);

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(clientSocket,clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect;
        if (!connectImap(inFromServer)) { // Imap 서버 연결 실패
            clientSocket.close();
            exit(1);
        }

        /*------ LOGIN 수행 -------*/
        if (!loginImap(outToServer, inFromServer)) { // 로그인 실패
            clientSocket.close();
            exit(1);
        }

        /*------ DELETE 수행 -------*/
        System.out.println("## Folder Delete ##");
        System.out.print("Select Folder to delete : ");
        String folderName = in.next();//폴더 이름 입력 받음

        String delete = "foo DELETE "+folderName+"\r\n";
        outToServer.write(delete.getBytes());

        isConnect = inFromServer.readLine();
        if (isConnect.startsWith("foo OK")){
            System.out.println("Folder Deleted");
            System.out.println();
        }
        else {
            if(isConnect.contains("Unknown folder"))
                System.out.println("\n"+"\'"+folderName+"\'"+" folder doesn't exist.");
            else
                System.out.println(isConnect);
            System.out.println("Failed to Delete Folder");
            clientSocket.close();
            exit(1);
        }

        clientSocket.close();
    }
    public void renameFolder() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        // imap.google.com에 연결하는 socket형성
        Socket clientSocket = new Socket(imapServer, imapPort);

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(clientSocket,clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect;
        if (!connectImap(inFromServer)) { // Imap 서버 연결 실패
            clientSocket.close();
            exit(1);
        }

        /*------ LOGIN 수행 -------*/
        if (!loginImap(outToServer, inFromServer)) { // 로그인 실패
            clientSocket.close();
            exit(1);
        }

        /*------ RENAME 수행 -------*/
        System.out.println("## Folder Rename ##");
        System.out.print("ex-Folder Name : ");
        String exfolderName = in.next();//폴더 이름 입력 받음
        System.out.print("new Folder Name : ");
        String folderName  = in.next();

        String rename = "foo RENAME "+exfolderName+" "+folderName+"\r\n";
        outToServer.write(rename.getBytes());

        isConnect = inFromServer.readLine();
        if (isConnect.startsWith("foo OK")){
            System.out.println("Rename completed");
            System.out.println();
        }
        else {
            System.out.println(isConnect);
            System.out.println("Rename failure");
            clientSocket.close();
            exit(1);
        }
        clientSocket.close();
    }
    public void getMailList() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        // imap.google.com에 연결하는 socket형성
        Socket clientSocket = new Socket(imapServer, imapPort);

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(clientSocket,clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect;
        if (!connectImap(inFromServer)) { // Imap 서버 연결 실패
            clientSocket.close();
            exit(1);
        }

        /*------ LOGIN 수행 -------*/
        if (!loginImap(outToServer, inFromServer)) { // 로그인 실패
            clientSocket.close();
            exit(1);
        }

        /*------ Folder 리스트 나열 -------*/
        System.out.println("## Get Folder List ##");
        String list="foo LIST "+"\"\""+" \"*\""+"\r\n";
        outToServer.write(list.getBytes());
        isConnect="init";
        while(!isConnect.startsWith("foo OK")) //명령 응답이 너무 많고 느려서 다음 명령 응답이랑 겹치기 때문에 명령 수행 결과를 확인할 수 없어서 OK가 나올 때까지 기다리는 것
        {
            isConnect=inFromServer.readLine();
            if(isConnect.contains("(\\HasNoChildren)")) // 폴더 리스트만 출력하기 위한 것
                System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))// 명령이 실패하였을 경우
            {
                System.out.println("Failed to Get Folder");
                clientSocket.close();
                return;
            }
        }
        System.out.println("Got Folder");   // Folder 리스트 나열 성공

        /*------ Folder 선택 -------*/
        System.out.println();
        System.out.println("## Get Mail List ##");
        System.out.print("Select Folder Name to Get Mail List : ");
        String selectFolder=in.next(); // 폴더 이름 입력 받기

        /*------ 선택한 폴더로 진입 -------*/
        String mailBox="foo SELECT "+selectFolder+"\r\n";
        outToServer.write(mailBox.getBytes());
        isConnect = inFromServer.readLine();
        System.out.println();
        System.out.println(isConnect);

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();
            System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to Select");
                clientSocket.close();
                return;
            }
        }
        System.out.println("Selected"); // 폴더 선택 성공

        /*------ 선택한 폴더의 Mail List Fetch -------*/
        String searchList = "foo FETCH 1:* (FLAGS BODY[HEADER.FIELDS (DATE SUBJECT)])\r\n"; //
        outToServer.write(searchList.getBytes());

        isConnect = inFromServer.readLine();
        System.out.println();
        System.out.println(isConnect);

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();

            /* 제목 디코딩 처리
             * 1. 첫번째 if 구문
             * " =?utf", " =?UTF", " =?euc", " =?EUC"로 시작하는 제목은 디코딩 처리가 필요
             * 위 부분과 맨뒤의 "?"부분을 잘라내고, 나머지 부분을 mimeDecoder를 이용하여 디코딩
             * 문자열의 끝부분 4-byte 조건(==)을 맞추기 위해 끝이 "="한개인 부분은 "="를 추가해줌
             * 2. 두번째 else if 구문
             * 제목이 길어 여러개의 세그먼트로 나눠져 오는 제목 처리 (확인결과 문자열 최대길이  93)
             * -> 첫번째로 온 제목만 출력하고 나머지는 출력하지 않음
             * 3. 세번째 else 구문
             * 디코딩이 필요없으면 그냥 출력
             * */
            if(isConnect.startsWith("Subject:")){
                int index = isConnect.indexOf(":");
                String subject = isConnect.substring(index+1);
                if(subject.startsWith(" =?")) {
                    while(subject.contains(" =?utf")||subject.contains(" =?UTF")||subject.contains(" =?EUC")||subject.contains(" =?euc"))
                    {
                        subject = subject.substring(subject.indexOf(" ")+1);
                        if(subject.startsWith("=?utf")||subject.startsWith("=?UTF"))
                            subject = subject.substring(10);
                        if(subject.startsWith("=?EUC")||subject.startsWith("=?euc"))
                            subject = subject.substring(11);
                    }

                    int idx = subject.indexOf("?");
                    subject = subject.substring(0, idx-1);
                    if( subject.substring(subject.length()-1,subject.length())!="==")
                        subject = subject + "=";

                    Decoder decoder = Base64.getMimeDecoder();
                    subject = new String(decoder.decode(subject),"UTF-8");
                }

                System.out.println(" Subject: "+ subject);

            }
            else if(isConnect.startsWith(" =?"))
            {
                while(isConnect.startsWith(" =?"))
                    isConnect = inFromServer.readLine();
            }
            else
                System.out.println(isConnect);

            //System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to Fetch");
                clientSocket.close();
                return;
            }
        }
        System.out.println("Fetched");
        clientSocket.close();

    }
    public void deleteMail() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        // imap.google.com에 연결하는 socket형성
        Socket clientSocket = new Socket(imapServer, imapPort);

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(clientSocket,clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect;
        if (!connectImap(inFromServer)) { // Imap 서버 연결 실패
            clientSocket.close();
            exit(1);
        }

        /*------ LOGIN 수행 -------*/
        if (!loginImap(outToServer, inFromServer)) { // 로그인 실패
            clientSocket.close();
            exit(1);
        }

        /*------ Mail Delete 수행 -------*/
        //1. 폴더 선택하기
        System.out.println("## Mail delete ##");
        System.out.print("Select Folder to Delete Mail : ");
        String folderName=in.next();// 폴더 이름 입력 받기
        String inbox="foo SELECT "+folderName+"\r\n";
        outToServer.write(inbox.getBytes());
        isConnect="init";

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();
            System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to List Folder");
                return;
            }
            if(isConnect.contains("* 0 EXISTS"))
            {
                System.out.println("Mail does not exist");
                clientSocket.close();
                return;

            }
        }

        //2. 선택한 폴더에서 삭제할 메일 선택하기
        System.out.println();
        System.out.println("Select Mail Index to Delete: ");
        int mailNumber=in.nextInt();// 삭제할 메일 인덱스 입력 받기

        String flag="foo STORE "+mailNumber+" +FLAGS \\Deleted\r\n"; // 삭제할 메일 \Deleted flag로 세팅
        outToServer.write(flag.getBytes());

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();
            System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to Select");
                clientSocket.close();
                return;
            }
        }
        System.out.println("Selected");

        //3. 선택한 메일 삭제하기
        String deleteMail="foo EXPUNGE\r\n";//\Deleted flag로 설정된 선택된 우편함에 있는 메일 영구 삭제
        outToServer.write(deleteMail.getBytes());
        isConnect="init";

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();
            System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to Delete");
                clientSocket.close();
                return;
            }
        }

        System.out.println("Deleted");  // delete message 성공
        clientSocket.close();
    }
    public void moveMail() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        // imap.google.com에 연결하는 socket형성
        Socket clientSocket = new Socket(imapServer, imapPort);

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(clientSocket,clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(), true);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect;
        if (!connectImap(inFromServer)) { // Imap 서버 연결 실패
            clientSocket.close();
            exit(1);
        }

        /*------ LOGIN 수행 -------*/
        if (!loginImap(outToServer, inFromServer)) { // 로그인 실패
            clientSocket.close();
            exit(1);
        }

        System.out.println("## Move Mail To Folder ##");

        /*------ Folder 리스트 나열 -------*/
        System.out.println("## Get Folder List ##");
        String list="foo LIST "+"\"\""+" \"*\""+"\r\n";
        outToServer.write(list.getBytes());
        isConnect="init";
        while(!isConnect.startsWith("foo OK")) //명령 응답이 너무 많고 느려서 다음 명령 응답이랑 겹치기 때문에 명령 수행 결과를 확인할 수 없어서 OK가 나올 때까지 기다리는 것
        {
            isConnect=inFromServer.readLine();
            if(isConnect.contains("(\\HasNoChildren)")) // 폴더 리스트만 출력하기 위한 것
                System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))// 명령이 실패하였을 경우
            {
                System.out.println("Failed to Get Folder");
                clientSocket.close();
                exit(1);
            }
        }
        System.out.println();
        System.out.print("Select Folder Name to Get Mail List : ");
        String selectFolder=in.next(); // 폴더 이름 입력 받기

        /*------ 선택한 폴더로 진입 -------*/
        String mailBox="foo SELECT "+selectFolder+"\r\n";
        outToServer.write(mailBox.getBytes());
        isConnect = inFromServer.readLine();
        System.out.println();
        System.out.println(isConnect);

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();
            System.out.println(isConnect);
            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to Select");
                clientSocket.close();
                exit(1);
            }
        }
        System.out.println("Selected");

        /*------ 선택한 폴더의 Mail List Fetch -------*/
        String searchList = "foo FETCH 1:* (FLAGS BODY[HEADER.FIELDS (DATE SUBJECT)])\r\n";
        outToServer.write(searchList.getBytes());

        isConnect = inFromServer.readLine();
        System.out.println();
        System.out.println(isConnect);

        while(!isConnect.startsWith("foo OK"))
        {
            isConnect=inFromServer.readLine();

            /* 제목 디코딩 처리
             * 1. 첫번째 if 구문
             * " =?utf", " =?UTF", " =?euc", " =?EUC"로 시작하는 제목은 디코딩 처리가 필요
             * 위 부분과 맨뒤의 "?"부분을 잘라내고, 나머지 부분을 mimeDecoder를 이용하여 디코딩
             * 문자열의 끝부분 4-byte 조건(==)을 맞추기 위해 끝이 "="한개인 부분은 "="를 추가해줌
             * 2. 두번째 else if 구문
             * 제목이 길어 여러개의 세그먼트로 나눠져 오는 제목 처리 (확인결과 문자열 최대길이  93)
             * -> 첫번째로 온 제목만 출력하고 나머지는 출력하지 않음
             * 3. 세번째 else 구문
             * 디코딩이 필요없으면 그냥 출력
             * */
            if(isConnect.startsWith("Subject:")){
                int index = isConnect.indexOf(":");
                String subject = isConnect.substring(index+1);
                if(subject.startsWith(" =?")) {
                    while(subject.contains(" =?utf")||subject.contains(" =?UTF")||subject.contains(" =?EUC")||subject.contains(" =?euc"))
                    {
                        subject = subject.substring(subject.indexOf(" ")+1);
                        if(subject.startsWith("=?utf")||subject.startsWith("=?UTF"))
                            subject = subject.substring(10);
                        if(subject.startsWith("=?EUC")||subject.startsWith("=?euc"))
                            subject = subject.substring(11);
                    }

                    int idx = subject.indexOf("?");
                    subject = subject.substring(0, idx-1);
                    if( subject.substring(subject.length()-1,subject.length())!="==")
                        subject = subject + "=";

                    Decoder decoder = Base64.getMimeDecoder();
                    subject = new String(decoder.decode(subject),"UTF-8");
                }

                System.out.println(" Subject: "+ subject);

            }
            else if(isConnect.startsWith(" =?"))
            {
                while(isConnect.startsWith(" =?"))
                    isConnect = inFromServer.readLine();
            }
            else
                System.out.println(isConnect);

            if(isConnect.startsWith("foo BAD")||isConnect.startsWith("foo NO"))
            {
                System.out.println("Failed to Fetch");
                clientSocket.close();
                exit(1);
            }
        }
        System.out.println("Fetched");  //fetch 성공
        System.out.println();

        /* 옮길 메일 원하는 폴더로 copy */
        System.out.print("Select Mail Index to Move : ");
        String selectMail=in.next(); // 옮길 메일 인덱스 번호 입력 받기

        System.out.print("Select Folder : ");
        String foldertoMove=in.next(); // 폴더명 입력 받기

        String mailtoMove="foo COPY "+selectMail+" "+foldertoMove+"\r\n"; // 선택한 메일을 선택한 폴더로 복사
        outToServer.write(mailtoMove.getBytes());
        isConnect = inFromServer.readLine();
        if(isConnect.contains("NO"))
            System.out.println("Move mail failed");
        else
            System.out.println("Move mail succeed");
        System.out.println();

        clientSocket.close();
    }
    private void sendMessage() throws Exception
    {
        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        /*------ 메일 정보 입력 ------*/
        System.out.print("Mail Receiver : ");
        String receiver = in.nextLine();
        System.out.print("Mail Subject : ");
        String subject = in.nextLine();
        System.out.print("Mail Message : ");
        String message = in.nextLine();

        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(smtpServer, smtpPort);

        // clientSocket에 Server로 보내는 버퍼 outToServer붙이기
        OutputStream outToServer = sslsocket.getOutputStream();
        // clientSocket에 Server로부터 입력을 받는 버퍼 InFromServer붙이기
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        /*------ 서버로부터의 응답 확인 -------*/
        String isConnect = inFromServer.readLine();
        if(isConnect.startsWith("220")){ // connection이 잘 되었을 때
            System.out.println();
            System.out.println("SMTP Server connection Accept");
        }
        else {   // connection이 안되었을때
            System.out.println("SMTP Server connection Failed");
            sslsocket.close();
            exit(1);
        }

        /*------ 1. EHLO 수행 -------*/ // 로그인 하려면 ESMTP(Extended SMTP) 사용해야함
        String ehlo = "EHLO "+mailserver+"\r\n";
        outToServer.write(ehlo.getBytes());

        do {
            isConnect = inFromServer.readLine();
            System.out.println(isConnect);
            if (!isConnect.startsWith("250")) {
                sslsocket.close();
                System.out.println("EHLO FAILED");
            }
        } while (isConnect.contains("-"));
        System.out.println();

        /*------ 2. AUTH LOGIN 수행 -------*/ // 메일 계정에 로그인 시도
        String auth = "AUTH LOGIN\r\n";
        System.out.print(auth);
        outToServer.write(auth.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("334")) {
            sslsocket.close();
            System.out.println("AUTH Failed");
        } else
            System.out.println("AUTH Accept");

        /*------ 3. 메일 계정 입력 -------*/
        String mailId = DatatypeConverter.printBase64Binary(id.getBytes())+"\r\n"; // AUTH는 모두 base64로 인코딩되어야함
        outToServer.write(mailId.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("334")) {
            sslsocket.close();
            System.out.println("ID Failed");
        } else
            System.out.println("ID Accept");

        /*------ 4. 메일 계정 비밀번호 입력 -------*/
        String mailPw = DatatypeConverter.printBase64Binary(pw.getBytes())+"\r\n"; // AUTH는 모두 base64로 인코딩되어야함
        outToServer.write(mailPw.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("235")) {
            sslsocket.close();
            System.out.println("PW Failed");
        } else
            System.out.println("PW Accept");

        /*------ 5. 메일 발신자 지정(MAIL FROM) -------*/
        String send = "MAIL FROM:<"+id+">\r\n";
        outToServer.write(send.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("250")) {
            sslsocket.close();
            System.out.println("MAIL FROM Failed");
        } else
            System.out.println("MAIL FROM Accept");

        /*------ 6. 메일 수신자 지정(RCPT TP) -------*/
        String recieve = "RCPT TO:<"+receiver+">\r\n";
        outToServer.write(recieve.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("250")) {
            sslsocket.close();
            System.out.println("RCPT TO Failed");
        } else
            System.out.println("RCPT TO Accept");

        /*------ 7. DATA 수행 -------*/
        String data = "DATA\r\n";
        outToServer.write(data.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("354")) {
            sslsocket.close();
            System.out.println("DATA Failed");
        } else
            System.out.println("DATA Accept");

        /*------ 8. 메일 내용 구성 -------*/
        String msgto = "to:<" +receiver+ ">\r\n";
        String msgfrom = "from:<"+id+">\r\n";
        String msgsub = "subject: "+subject+"\r\n";
        String temp = "\n"; // msg본분을 입력하기전에 enter가 있어야함.
        message+="\r\n";    // msg는 입력을 받기 때문에 \r\n이 없음. 그래서 만들어줌
        String end = ".\r\n"; // msg 끝남을 알림

        outToServer.write(msgto.getBytes());    // 수신자
        outToServer.write(msgfrom.getBytes());  // 발신자
        outToServer.write(msgsub.getBytes());   // 제목
        outToServer.write(temp.getBytes());     // enter
        outToServer.write(message.getBytes());  // 메일 내용
        outToServer.write(end.getBytes());      // DATA 끝남을 알림

        isConnect = inFromServer.readLine(); // DATA 수신에 대한 응답 받음
        if (!isConnect.startsWith("250")) {
            sslsocket.close();
            System.out.println("MSG Failed");
        } else
            System.out.println("MSG Accept");

        /*------ 9. QUIT 수행 -------*/
        String quit = "QUIT\r\n";
        outToServer.write(quit.getBytes());

        isConnect = inFromServer.readLine();
        if (!isConnect.startsWith("221")) {
            sslsocket.close();
            System.out.println("QUIT Failed");
        } else
            System.out.println("QUIT Accept");

        sslsocket.close();

    }

    public static void main(String argc[]) throws Exception
    {
        mailClient client = new mailClient();

        Scanner in = new Scanner(System.in);//입력을 받기위한 scanner

        //login하기 위한 id와 password입력
        System.out.println("## Login ##");
        System.out.print("id : ");
        client.id = in.next();
        System.out.print("password : ");
        client.pw = in.next();

        // host 만들기
        client.makeimaphost(client.id);
        client.makesmtphost(client.id);

        while(true) {   // 메뉴
            System.out.println();
            System.out.println();
            System.out.println("\t## menu ##");
            System.out.println("1. create folder");
            System.out.println("2. delete folder");
            System.out.println("3. rename folder");
            System.out.println("4. get mail list");
            System.out.println("5. delete mail");
            System.out.println("6. move mail to folder");
            System.out.println("7. send message");
            System.out.println("0. exit");
            System.out.println("Press number what you want");

            int menu = in.nextInt();

            switch (menu) {
                case (1):
                    client.createFolder();
                    break;
                case (2):
                    client.deleteFolder();
                    break;
                case (3):
                    client.renameFolder();
                    break;
                case (4):
                    client.getMailList();
                    break;
                case (5):
                    client.deleteMail();
                    break;
                case (6):
                    client.moveMail();
                    break;
                case(7):
                    client.sendMessage();
                    break;
                case(0):
                    exit(1);
                default:
                    break;
            }
        }
    }
}
// getBytes() -> default : UTF-8
// UTF-8 유니코드는 아스키 코드와 영문 영역에서는 100% 호환됩니다. 만약 UTF-8 유니코드 문서에 한글 등이 전혀 없고,
// 영문과 숫자로만 이루어져 있다면, 그 파일은 아스키 코드와 동일합니다.
// 웹페이지를 유니코드로 만들 때에는 UTF-8 유니코드를 사용합니다.

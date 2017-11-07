package ru.mail.polis.mikhail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;

public class RequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        boolean master = false;
        String requestMethod = httpExchange.getRequestMethod();
        String query = httpExchange.getRequestURI().getQuery();
        String id;
        String replicas;
        int ack = -1;
        int from = -1;
        if(!httpExchange.getRequestHeaders().containsKey("Replication")){
            master = true;
        }
        if(query.contains("&")){
            String[] parameters = query.split("&");
            id = parameters[0];
            replicas = parameters[1];
            if(id.split("=").length==1){
                sendHttpResponse(httpExchange,400,"Empty ID");
                return;
            }
            id = id.split("=")[1];
            if(replicas.split("=").length==1){
                sendHttpResponse(httpExchange,400,"Empty replicas");
                return;
            }
            replicas = replicas.split("=")[1];
            ack = Integer.valueOf(replicas.split("/")[0]);
            from = Integer.valueOf(replicas.split("/")[1]);
            if(ack>from || ack ==0 || from ==0){
                sendHttpResponse(httpExchange,400,"Invalid parameters");
                return;
            }
        }
        else{
            if(query.split("=").length==1){
                sendHttpResponse(httpExchange,400,"Empty ID");
                return;
            }
            id = query.split("=")[1];
        }
        if(ack==-1 || from==-1){
            ack = topology.size()/2+1;
            from = topology.size();
        }
        File file = new File(data.getAbsolutePath()+delimiter+id);
        if(requestMethod.equalsIgnoreCase("GET")){
            if (master) {
                ReplicationManager rm = new ReplicationManager(topology, ack,from, query, "GET", "http:/" + httpExchange.getLocalAddress().toString(), null);
                int status = rm.replication();
                if (status==0) {
                    if(!file.exists()){
                        sendHttpResponse(httpExchange,404,"Not found");
                        return;
                    }
                    sendHttpResponse(httpExchange, 200, file);
                } else if(status==-1) {
                    sendHttpResponse(httpExchange, 504, "Not Enough Replicas");
                }
                else{
                    sendHttpResponse(httpExchange,404,"Not found");
                }
            }
            else{
                if(!file.exists()){
                    sendHttpResponse(httpExchange,404,"Not found");
                    return;
                }
                sendHttpResponse(httpExchange, 200, "OK");
            }
        }
        else if(requestMethod.equalsIgnoreCase("PUT")){
            if(!file.exists()) file.createNewFile();
            byte[] buffer = new byte[1024];
            InputStream is = httpExchange.getRequestBody();
            BufferedOutputStream bs = new BufferedOutputStream(new FileOutputStream(file));
            for(int n=is.read(buffer);n>0; n=is.read(buffer))  bs.write(buffer);
            bs.close();
            if(master){
                ReplicationManager rm = new ReplicationManager(topology, ack,from, query,"PUT", "http:/"+httpExchange.getLocalAddress().toString(), buffer);
                if(rm.replication()==0){
                    sendHttpResponse(httpExchange, 201, "Created");
                }
                else{
                    sendHttpResponse(httpExchange, 504, "Not Enough Replicas");
                }
            }
            else {
                sendHttpResponse(httpExchange, 201, "Created");
            }
        }
        else if(requestMethod.equalsIgnoreCase("DELETE")){
            if(!file.exists() || file.delete()) {
                if (master) {
                    ReplicationManager rm = new ReplicationManager(topology, ack, from, query, "DELETE", "http:/" + httpExchange.getLocalAddress().toString(), null);
                    if (rm.replication()==0) {
                        sendHttpResponse(httpExchange, 202, "Accepted");
                    } else {
                        sendHttpResponse(httpExchange, 504, "Not Enough Replicas");
                    }
                }
                else{
                    sendHttpResponse(httpExchange, 202, "Accepted");
                }
            }
            else{
                throw new IOException();
            }
        }
        else{
            throw new IOException();
        }
    }
}
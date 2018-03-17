package de.cmt.cometportable.util;

import java.io.IOException;
import java.net.InetAddress;

import de.cmt.cometportable.test.domain.Environment;
import de.cmt.cometportable.test.domain.EnvironmentAuthenticationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;


public class SSHConnection {

    private final Logger log = LoggerFactory.getLogger(SSHConnection.class);

    Environment env;

    public SSHConnection(Environment environment) {
        this.env = environment;
    }

    public boolean isHostReachable() {

        boolean isReachable = false;

        if(this.env.getHost() != null && !this.env.getHost().isEmpty()) {
            String host = this.env.getHost();

            if(host.indexOf('@') >= 0) {
                host = env.getHost().substring(host.indexOf('@')+1);
            }

            try {
                isReachable = InetAddress.getByName(host).isReachable(2000);
            } catch(IOException e) {
                this.log.error("Unhandled SSH Connection Error (IOException) occurred");
                this.log.error("IOException %s", e);
            }
        }

        return isReachable;
    }

    public boolean hasValidAuthentication() {

        boolean validAuthentication = true;

        String host = this.env.getHost();
        String port = "22";

        if(this.env.getHost().indexOf(':') >= 0) {
            host = env.getHost().substring(0, this.env.getHost().indexOf(':'));
            port= env.getHost().substring(host.indexOf(':')+1);
        }

        JSch jsch = new JSch();

        if(env.getAuthenticationType() != EnvironmentAuthenticationType.WINRM_PASSWORD) {
            if(isHostReachable()) {

                try {

                    if(env.getAuthenticationType() == EnvironmentAuthenticationType.KEY) {
                        jsch.addIdentity(this.env.getKeyFile());
                    }

                    Session session=jsch.getSession(env.getUser() , host, Integer.parseInt(port));

                    if(env.getAuthenticationType() == EnvironmentAuthenticationType.PASSWORD) {
                        session.setPassword(this.env.getPassword().getBytes());
                    }

                    //to avoid user info popup dialog ..
                    java.util.Properties config = new java.util.Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);

                    try {
                        session.connect();

                        // Catch java.net.ConnectException
                    } catch (Exception e) {
                        log.error("Cannot connect to Environment");
                    }


                    Channel channel = session.openChannel("exec");
                    ChannelExec ssh = (ChannelExec) channel;
                    ssh.setCommand("hostname");
                    ssh.connect();

                    channel.disconnect();
                    session.disconnect();

                } catch(NumberFormatException | JSchException e) {
                    this.log.error("Establishing connection to host {} failed. Reason:", this.env.getHost());
                    this.log.error("JSchException or NumberFormatException %s", e);

                    validAuthentication = false;
                }

            } else {
                validAuthentication = false;
            }
        } else {
            validAuthentication = isHostReachable();
        }

        return validAuthentication;
    }

    public static KeyPair generateKeyPair()  {
        return SSHConnection.generateKeyPair(2048);
    }

    public static KeyPair generateKeyPair(int keyLength)  {

        Logger log = LoggerFactory.getLogger(SSHConnection.class);
        JSch jSSH = new JSch();

        KeyPair pair = null;

        try {
            pair = KeyPair.genKeyPair(jSSH, KeyPair.RSA, keyLength);
        } catch(JSchException e) {
            log.error("Unhandled SSH Key Generation Error (JSchException) occurred");
            log.error("JSchException %s", e);
        }

        return pair;
    }
}

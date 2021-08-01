package com.company;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import javax.swing.*;
import java.util.Properties;

public class AWSDatabaseConnect {

    public static String DRIVER = "org.postgresql.Driver";
    public static String DATABASE_HOST = "database-1.claccpgo40zh.ap-southeast-1.rds.amazonaws.com";
    public static int DATABASE_PORT = 5432;
    public static String USERNAME = "sparkdev";
    public static String PASSWORD = "Welcome2021!";

    public static DataSource getDataSource(int forwardedPort) {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER);
        String url = "jdbc:postgresql://localhost:" + forwardedPort + "/postgres";
        dataSource.setUrl(url);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        return dataSource;
    }
    public static void main(String[] args) throws JSchException {

        JSch jsch = new JSch();

        // This is the private key given by AWS when creating the EC2 instance that act as SSH server
        jsch.addIdentity("C:/keys/NickSSHServer.pem", "");

        /* Run the following command in Cygwin and put the output in a file
         *   ssh-keyscan -t rsa 13.229.152.78
         */
        jsch.setKnownHosts("C:/keys/aws_hosts");

        Session session = jsch.getSession("ec2-user", "13.229.152.78", 22);

        session.connect();

        // Forward randomly chosen local port through the SSH channel to database host/port
        int forwardedPort = session.setPortForwardingL(0, DATABASE_HOST, DATABASE_PORT);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource(forwardedPort));
        System.out.println(getCount(jdbcTemplate));
    }

    public static int getCount(JdbcTemplate template) {
        return template.queryForObject("select count(*) from fx.fx_order_all", Integer.class);
    }
}

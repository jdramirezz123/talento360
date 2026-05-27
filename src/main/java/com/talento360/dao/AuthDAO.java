package com.talento360.dao;

import com.talento360.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private String currentUsername = "admin@boyaca.gov.co";
    private String currentName = "Administrador Talento Humano";
    private String currentRole = "Administrador";
    private String currentJobTitle = "Administrador del sistema";
    private String currentDepartment = "Dirección de Talento Humano";

    public boolean login(String username, String password) {
        String normalizedUser = normalizeUser(username);
        String normalizedPass = password == null ? "" : password.trim();
        String dbUsername = canonicalUsername(normalizedUser);
        String sql = "SELECT username, nombre, rol FROM usuarios WHERE LOWER(username) = ? AND password = ? AND estado = 'ACTIVO'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dbUsername);
            stmt.setString(2, normalizedPass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUsername = rs.getString("username");
                currentName = rs.getString("nombre");
                currentRole = rs.getString("rol");
                applyProfileFor(currentUsername, currentRole);
                return true;
            }
        } catch (Throwable e) {
            System.err.println("PostgreSQL login error: " + e.getMessage());
            return loginDemo(normalizedUser, normalizedPass);
        }
        return loginDemo(normalizedUser, normalizedPass);
    }

    private boolean loginDemo(String username, String password) {
        Map<String, String[]> demoUsers = new HashMap<>();

        addDemoUser(demoUsers,
                new String[]{"admin", "admin@boyaca.gov.co"},
                new String[]{"admin123", "Administrador Talento Humano", "Administrador", "Administrador del sistema", "Dirección de Talento Humano", "admin@boyaca.gov.co"});

        addDemoUser(demoUsers,
                new String[]{"carlos", "carlos.aguilar", "carlos.aguilar@boyaca.gov.co"},
                new String[]{"carlos123", "Carlos Aguilar", "Coordinador Talento Humano", "Coordinador de solicitudes", "Dirección de Talento Humano", "carlos.aguilar@boyaca.gov.co"});

        addDemoUser(demoUsers,
                new String[]{"maria", "maria.gonzales", "maria.gonzalez", "maria.gonzales@boyaca.gov.co", "maria.gonzalez@boyaca.gov.co"},
                new String[]{"maria123", "Maria Gonzales", "Consulta", "Usuario de consulta", "Secretaría General", "maria.gonzales@boyaca.gov.co"});

        String[] data = demoUsers.get(username);
        if (data != null && data[0].equals(password)) {
            currentUsername = data[5];
            currentName = data[1];
            currentRole = data[2];
            currentJobTitle = data[3];
            currentDepartment = data[4];
            return true;
        }
        return false;
    }

    private void addDemoUser(Map<String, String[]> demoUsers, String[] usernames, String[] data) {
        for (String username : usernames) {
            demoUsers.put(normalizeUser(username), data);
        }
    }

    private String normalizeUser(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private String canonicalUsername(String username) {
        return switch (username) {
            case "admin" -> "admin@boyaca.gov.co";
            case "carlos", "carlos.aguilar" -> "carlos.aguilar@boyaca.gov.co";
            case "maria", "maria.gonzales", "maria.gonzalez", "maria.gonzalez@boyaca.gov.co" -> "maria.gonzales@boyaca.gov.co";
            default -> username;
        };
    }

    private void applyProfileFor(String username, String role) {
        String user = username == null ? "" : username.toLowerCase();
        if (user.contains("carlos.aguilar")) {
            currentJobTitle = "Coordinador de solicitudes";
            currentDepartment = "Dirección de Talento Humano";
        } else if (user.contains("maria.gonzales") || user.contains("maria.gonzalez")) {
            currentJobTitle = "Usuario de consulta";
            currentDepartment = "Secretaría General";
        } else if (role != null && role.toLowerCase().contains("coordinador")) {
            currentJobTitle = "Coordinador de solicitudes";
            currentDepartment = "Dirección de Talento Humano";
        } else if (role != null && role.toLowerCase().contains("consulta")) {
            currentJobTitle = "Usuario de consulta";
            currentDepartment = "Secretaría General";
        } else {
            currentJobTitle = "Administrador del sistema";
            currentDepartment = "Dirección de Talento Humano";
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentName() {
        return currentName;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public String getCurrentJobTitle() {
        return currentJobTitle;
    }

    public String getCurrentDepartment() {
        return currentDepartment;
    }

    public boolean canEditRequests() {
        return "Administrador".equalsIgnoreCase(currentRole) || currentRole.toLowerCase().contains("coordinador");
    }

    public boolean canCreateRequests() {
        return canEditRequests();
    }
}

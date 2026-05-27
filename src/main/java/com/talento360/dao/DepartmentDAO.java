package com.talento360.dao;

import com.talento360.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DepartmentDAO {
    public List<Map<String, String>> list(String filter) {
        List<Map<String, String>> rows = new ArrayList<>();
        String sql = """
                SELECT d.id_dependencia,
                       COALESCE(d.dependencia, '') AS dependencia,
                       COALESCE(ca.cargo, 'DIRECTIVO / COORDINADOR DE AREA') AS cargo,
                       COALESCE(p.nombre_completo, 'RESPONSABLE INSTITUCIONAL') AS nombre,
                       COALESCE(con.correo_institucional, '') AS correo,
                       COALESCE(con.telefono_fijo, '') AS extension,
                       'Gestion institucional, seguimiento administrativo y apoyo a los procesos de talento humano.' AS funciones
                FROM dependencias d
                LEFT JOIN rel_principal r ON r.id_registro = (
                    SELECT rp.id_registro
                    FROM rel_principal rp
                    WHERE rp.id_dependencia = d.id_dependencia
                    ORDER BY rp.id_registro
                    LIMIT 1
                )
                LEFT JOIN personas p ON p.id_persona = r.id_persona
                LEFT JOIN cargos ca ON ca.id_cargo = r.id_cargo_actual
                LEFT JOIN contactos con ON con.id_contacto = r.id_contacto
                ORDER BY d.id_dependencia
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("Dependencia", clean(rs.getString("dependencia")));
                row.put("Cargo", clean(rs.getString("cargo")));
                row.put("Nombre", clean(rs.getString("nombre")));
                row.put("Correo", departmentEmail(rs.getString("correo"), rs.getString("id_dependencia")));
                row.put("Extension", departmentExtension(rs.getString("extension"), rs.getString("id_dependencia")));
                row.put("Funciones", clean(rs.getString("funciones")));
                row.put("Ciudad", "Tunja, Boyaca");
                row.put("Estado", "Activo");
                rows.add(row);
            }
        } catch (Exception e) {
            System.err.println("Error listing departments: " + e.getMessage());
        }

        if (filter != null && !filter.isBlank()) {
            String f = filter.toLowerCase(Locale.ROOT);
            rows = rows.stream()
                    .filter(r -> r.values().stream().anyMatch(v -> v != null && v.toLowerCase(Locale.ROOT).contains(f)))
                    .toList();
        }
        return rows;
    }

    public List<String> listDepartmentNames() {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT dependencia FROM dependencias ORDER BY dependencia";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String value = clean(rs.getString("dependencia"));
                if (!value.isBlank() && !rows.contains(value)) rows.add(value);
            }
        } catch (Exception e) {
            System.err.println("Error listing department catalog: " + e.getMessage());
        }
        if (rows.isEmpty()) rows.addAll(demoDepartments());
        return rows;
    }

    public List<String> listJobTitleNames() {
        List<String> rows = new ArrayList<>();
        String sql = "SELECT cargo FROM cargos ORDER BY cargo";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String value = clean(rs.getString("cargo"));
                if (!value.isBlank() && !rows.contains(value)) rows.add(value);
            }
        } catch (Exception e) {
            System.err.println("Error listing job-title catalog: " + e.getMessage());
        }
        if (rows.isEmpty()) rows.addAll(demoJobTitles());
        return rows;
    }

    public boolean create(String name) {
        if (name == null || name.isBlank()) return false;
        String cleanName = clean(name);
        try (Connection conn = Database.getConnection()) {
            if (findDepartmentId(conn, cleanName) != null) return false;
            String id = nextDepartmentId(conn);
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO dependencias(id_dependencia, dependencia) VALUES (?, ?)")) {
                stmt.setString(1, id);
                stmt.setString(2, cleanName);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error creating department: " + e.getMessage());
            return false;
        }
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) return "NO REGISTRADO";
        return value.trim().replaceAll("\\s+", " ").toUpperCase(new Locale("es", "CO"));
    }

    private String departmentEmail(String value, String id) {
        if (value != null && !value.isBlank()) return value.trim().toLowerCase(Locale.ROOT);
        return "dependencia" + numericId(id) + "@boyaca.gov.co";
    }

    private String departmentExtension(String value, String id) {
        if (value != null && !value.isBlank()) return value.trim();
        return "22" + numericId(id);
    }

    private String numericId(String id) {
        String digits = id == null ? "" : id.replaceAll("\\D", "");
        if (digits.isBlank()) return "00";
        return digits.length() >= 2 ? digits.substring(digits.length() - 2) : "0" + digits;
    }

    private String findDepartmentId(Connection conn, String name) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id_dependencia FROM dependencias WHERE LOWER(dependencia) = LOWER(?) LIMIT 1")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private String nextDepartmentId(Connection conn) throws Exception {
        int max = 0;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_dependencia FROM dependencias")) {
            while (rs.next()) {
                String digits = rs.getString(1) == null ? "" : rs.getString(1).replaceAll("\\D", "");
                if (!digits.isBlank()) max = Math.max(max, Integer.parseInt(digits));
            }
        }
        return "DEP" + String.format("%03d", max + 1);
    }

    private List<String> demoDepartments() {
        return List.of(
                "DESPACHO DEL GOBERNADOR",
                "SECRETARIA DE PLANEACION",
                "SECRETARIA DE HACIENDA",
                "SECRETARIA GENERAL",
                "SECRETARIA DE GOBIERNO Y ACCION COMUNAL",
                "SECRETARIA DE EDUCACION",
                "SECRETARIA DE SALUD",
                "SECRETARIA DE INFRAESTRUCTURA PUBLICA",
                "DIRECCION DE TALENTO HUMANO",
                "OFICINA JURIDICA"
        );
    }

    private List<String> demoJobTitles() {
        return List.of(
                "PROFESIONAL UNIVERSITARIO",
                "PROFESIONAL ESPECIALIZADO",
                "TECNICO ADMINISTRATIVO",
                "AUXILIAR ADMINISTRATIVO",
                "ASESOR JURIDICO",
                "COORDINADOR DE AREA",
                "CONTRATISTA DE APOYO",
                "SECRETARIO DE DESPACHO"
        );
    }
}

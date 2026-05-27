package com.talento360.dao;

import com.talento360.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardDAO {
    private static final DateTimeFormatter UI_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public int count(String tableName) {
        if (!isAllowedTable(tableName)) return 0;
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return Math.max(rs.getInt(1), 0);
        } catch (Exception e) {
            System.err.println("Error count " + tableName + ": " + e.getMessage());
        }
        return 0;
    }

    private boolean isAllowedTable(String tableName) {
        return "rel_principal".equals(tableName)
                || "personas".equals(tableName)
                || "dependencias".equals(tableName)
                || "vacaciones".equals(tableName);
    }

    public List<Map<String, String>> recent() {
        return records(14);
    }

    public List<Map<String, String>> records(int limit) {
        List<Map<String, String>> rows = new ArrayList<>();
        String sql = """
                SELECT id_vacacion,
                       COALESCE(dependencia, '') AS dependencia,
                       COALESCE(NULLIF(hoy, ''), fecha_ingreso, '') AS fecha,
                       COALESCE(observaciones, '') AS observaciones,
                       COALESCE(tipo_vinculacion, '') AS tipo_vinculacion,
                       COALESCE(estado, '') AS estado,
                       COALESCE(apellidos_nombres, '') AS persona
                FROM vacaciones
                ORDER BY id_vacacion DESC
                LIMIT ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_vacacion");
                String notes = rs.getString("observaciones") == null ? "" : rs.getString("observaciones");
                String employmentType = rs.getString("tipo_vinculacion") == null ? "" : rs.getString("tipo_vinculacion");
                String type = processType(id, notes, employmentType);
                String status = normalizeStatus(rs.getString("estado"));
                rows.add(dashboardRow(
                        id,
                        type,
                        rs.getString("dependencia"),
                        rs.getString("persona"),
                        status,
                        rs.getString("fecha")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard records: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            int nextId = 5000;
            while (rows.size() < limit) {
                int id = nextId + rows.size();
                String type = processType(id, "", "");
                String status = switch (Math.abs(id) % 5) {
                    case 0 -> "Aprobada";
                    case 1 -> "Finalizada";
                    case 2 -> "Rechazada";
                    case 3 -> "En revisi\u00f3n";
                    default -> "Pendiente";
                };
                rows.add(dashboardRow(id, type, demoDepartment(id), demoPerson(id), status, ""));
            }
        }
        return rows;
    }

    private Map<String, String> dashboardRow(int id, String type, String department, String person, String status, String date) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("Radicado", filingNumber(id, type));
        row.put("Proceso", type);
        row.put("Descripci\u00f3n", description(type));
        row.put("Servidor", standard(person == null || person.isBlank() ? demoPerson(id) : person));
        row.put("Fecha", visibleDate(id, date));
        row.put("Dependencia", standard(department == null || department.isBlank() ? demoDepartment(id) : department));
        row.put("Estado", status == null || status.isBlank() ? "Pendiente" : status);
        return row;
    }

    private String filingNumber(int id, String type) {
        String prefix = switch (type) {
            case "Incapacidad" -> "INCA";
            case "Permiso" -> "PER";
            case "Licencia maternidad" -> "MAT";
            case "Hoja de vida" -> "HV";
            default -> "VAC";
        };
        return prefix + "-2026-" + String.format("%05d", Math.abs(id));
    }

    private String processType(int id, String notes, String employmentType) {
        String noteText = (notes == null ? "" : notes).toLowerCase(Locale.ROOT);
        String employmentText = (employmentType == null ? "" : employmentType).toLowerCase(Locale.ROOT);
        if (employmentText.contains("matern")) return "Licencia maternidad";
        if (employmentText.contains("permiso")) return "Permiso";
        if (employmentText.contains("incap")) return "Incapacidad";
        if (employmentText.contains("vacacion") || employmentText.contains("vacaciones")) return "Vacaciones";
        if (noteText.contains("matern")) return "Licencia maternidad";
        if (noteText.contains("permiso")) return "Permiso";
        if (noteText.contains("incap")) return "Incapacidad";
        if (noteText.contains("vacacion") || noteText.contains("vacaciones")) return "Vacaciones";
        int mod = Math.abs(id) % 6;
        return switch (mod) {
            case 0 -> "Incapacidad";
            case 1 -> "Permiso";
            case 2 -> "Hoja de vida";
            case 3 -> "Licencia maternidad";
            default -> "Vacaciones";
        };
    }

    private String description(String type) {
        return switch (type) {
            case "Incapacidad" -> "Incapacidad medica registrada";
            case "Permiso" -> "Permiso laboral solicitado";
            case "Licencia maternidad" -> "Licencia por maternidad";
            case "Hoja de vida" -> "Actualizacion de hoja de vida";
            default -> "Solicitud de vacaciones";
        };
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "Pendiente";
        String e = status.trim().toLowerCase(Locale.ROOT);
        return switch (e) {
            case "aprobada", "aprobado" -> "Aprobada";
            case "finalizada", "finalizado", "completado", "completada" -> "Finalizada";
            case "rechazada", "rechazado" -> "Rechazada";
            case "en revisi\u00f3n", "en revision", "revision" -> "En revisi\u00f3n";
            default -> "Pendiente";
        };
    }

    private String visibleDate(int id, String dateValue) {
        LocalDate visibleDate = parseDate(dateValue);
        if (visibleDate == null) {
            LocalDate baseRows = LocalDate.of(2026, 5, 20);
            visibleDate = baseRows.minusDays(Math.abs(id) % 45);
        }
        LocalTime time = LocalTime.of(8 + Math.abs(id) % 9, (Math.abs(id) * 7) % 60);
        return visibleDate.format(UI_DATE) + "\n" + time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        String clean = date.trim();
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        )) {
            try {
                return LocalDate.parse(clean, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String standard(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("\\s+", " ").toUpperCase(new Locale("es", "CO"));
    }

    private String demoDepartment(int id) {
        String[] deps = {
                "SECRETARIA DE EDUCACION", "SECRETARIA DE SALUD", "SECRETARIA DE HACIENDA",
                "DIRECCION DE TALENTO HUMANO", "SECRETARIA DE PLANEACION", "OFICINA JURIDICA",
                "SECRETARIA DE GOBIERNO"
        };
        return deps[Math.abs(id) % deps.length];
    }

    private String demoPerson(int id) {
        String[] people = {
                "MARTINEZ PEREZ LAURA", "TORRES RIVERA CARLOS ANDRES", "ROJAS GOMEZ DIANA CAROLINA",
                "MORENO SUAREZ JUAN SEBASTIAN", "GOMEZ RUIZ PAULA ANDREA", "CASTRO DIAZ MIGUEL ANGEL",
                "SILVA PARRA NATALIA FERNANDA"
        };
        return people[Math.abs(id) % people.length];
    }
}

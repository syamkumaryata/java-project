package edu.busTracker.dao;

import edu.busTracker.db.ConnectionProvider;
import edu.busTracker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentDao extends GenericJdbcDao<Student, String> {

    @Autowired
    public StudentDao(ConnectionProvider cp) {
        super(cp);
    }

    @Override
    protected String tableName() { return "students"; }

    @Override
    protected RowMapper<Student> rowMapper() {
        return rs -> {
            Student s = new Student();
            s.setRegNo(rs.getString("reg_no"));
            s.setName(rs.getString("name"));
            s.setEmail(rs.getString("email"));
            s.setGender(rs.getString("gender"));
            s.setBatch(rs.getObject("batch") != null ? rs.getInt("batch") : null);
            s.setSchool(rs.getString("school"));
            s.setProgram(rs.getString("program"));
            s.setFeeStatus(rs.getString("fee_status"));
            s.setTransportActive(rs.getInt("transport_active") == 1);
            s.setDemo(rs.getInt("is_demo") == 1);
            return s;
        };
    }

    @Override
    protected String findByIdSql() {
        return "SELECT * FROM students WHERE reg_no = ?";
    }

    @Override
    protected void bindId(PreparedStatement ps, String regNo) throws SQLException {
        ps.setString(1, regNo);
    }

    public Optional<Student> findByEmail(String email) {
        return queryOne("SELECT * FROM students WHERE email = ?",
                ps -> ps.setString(1, email));
    }

    public List<Student> findDemoStudents() {
        return queryList("SELECT * FROM students WHERE is_demo = 1 ORDER BY reg_no", null);
    }

    @Override
    public Student save(Student student) {
        String sql = """
                INSERT INTO students(reg_no, email, name, gender, batch, school, program,
                                     fee_status, transport_active, is_demo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        execute(sql, ps -> {
            ps.setString(1, student.getRegNo());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getName());
            ps.setString(4, student.getGender());
            if (student.getBatch() != null) ps.setInt(5, student.getBatch());
            else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setString(6, student.getSchool());
            ps.setString(7, student.getProgram());
            ps.setString(8, student.getFeeStatus());
            ps.setInt(9, student.isTransportActive() ? 1 : 0);
            ps.setInt(10, student.isDemo() ? 1 : 0);
        });
        return student;
    }

    public void upsert(Student student) {
        String sql;
        if (connectionProvider.isMySql()) {
            sql = """
                INSERT INTO students(reg_no, email, name, gender, batch, school, program,
                                     fee_status, transport_active, is_demo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    email = VALUES(email),
                    name = VALUES(name),
                    fee_status = VALUES(fee_status),
                    transport_active = VALUES(transport_active)
                """;
        } else {
            sql = """
                INSERT INTO students(reg_no, email, name, gender, batch, school, program,
                                     fee_status, transport_active, is_demo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(reg_no) DO UPDATE SET
                    email = excluded.email,
                    name = excluded.name,
                    fee_status = excluded.fee_status,
                    transport_active = excluded.transport_active
                """;
        }
        execute(sql, ps -> {
            ps.setString(1, student.getRegNo());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getName());
            ps.setString(4, student.getGender());
            if (student.getBatch() != null) ps.setInt(5, student.getBatch());
            else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setString(6, student.getSchool());
            ps.setString(7, student.getProgram());
            ps.setString(8, student.getFeeStatus());
            ps.setInt(9, student.isTransportActive() ? 1 : 0);
            ps.setInt(10, student.isDemo() ? 1 : 0);
        });
    }

    @Override
    public void update(Student student) {
        String sql = """
                UPDATE students SET email=?, name=?, gender=?, batch=?, school=?, program=?,
                    fee_status=?, transport_active=?
                WHERE reg_no=?
                """;
        execute(sql, ps -> {
            ps.setString(1, student.getEmail());
            ps.setString(2, student.getName());
            ps.setString(3, student.getGender());
            if (student.getBatch() != null) ps.setInt(4, student.getBatch());
            else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, student.getSchool());
            ps.setString(6, student.getProgram());
            ps.setString(7, student.getFeeStatus());
            ps.setInt(8, student.isTransportActive() ? 1 : 0);
            ps.setString(9, student.getRegNo());
        });
    }

    @Override
    public boolean deleteById(String regNo) {
        return execute("DELETE FROM students WHERE reg_no = ?",
                ps -> ps.setString(1, regNo)) > 0;
    }
}

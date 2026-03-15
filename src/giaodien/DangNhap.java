package giaodien;

import java.awt.EventQueue;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import database.DBConnection;

public class DangNhap extends JFrame {

    private JPanel contentPane;
    private JTextField txtTDN;
    private JPasswordField txtMK;

    
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DangNhap frame = new DangNhap();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DangNhap() {
        setTitle("ĐĂNG NHẬP");  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 513, 278);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblUser.setBounds(33, 45, 137, 31);
        contentPane.add(lblUser);

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblPass.setBounds(69, 84, 85, 31);
        contentPane.add(lblPass);

        txtTDN = new JTextField();
        txtTDN.setBounds(162, 47, 267, 31);
        contentPane.add(txtTDN);

        txtMK = new JPasswordField();
        txtMK.setBounds(162, 86, 267, 31);
        contentPane.add(txtMK);

        JButton btnDangNhap = new JButton("Đăng nhập");
        btnDangNhap.setBounds(162, 139, 124, 42);
        contentPane.add(btnDangNhap);

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setBounds(345, 139, 85, 42);
        contentPane.add(btnThoat);
        
        this.getRootPane().setDefaultButton(btnDangNhap);

        // ========= SỰ KIỆN =========
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        btnThoat.addActionListener(e -> System.exit(0));
    }

    private void xuLyDangNhap() {
        String username = txtTDN.getText().trim();
        String password = new String(txtMK.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT role FROM users WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");

                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                TrangChu tc = new TrangChu(role);
                tc.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL!");
        }
    }
}

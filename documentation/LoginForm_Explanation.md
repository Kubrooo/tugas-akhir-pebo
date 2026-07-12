# Penjelasan Kode: `LoginForm.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [LoginForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/LoginForm.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Penjaga Pintu dan Buku Tamu Gedung Perkantoran**
>
> Bayangkan sebuah gedung perkantoran eksklusif bernama **OriTeh Sapuro**. Gedung ini memiliki pintu masuk yang dijaga oleh seorang **Penjaga Keamanan** (Security).
> - **Input Form (`tfUsername` & `tfPassword`):** Pengunjung yang ingin masuk harus menyebutkan nama panggilan (Username) dan kata sandi rahasia hari itu (Password).
> - **Proses Verifikasi (`SQL SELECT`):** Penjaga tidak langsung percaya. Ia mencocokkan nama dan sandi tersebut ke dalam **Buku Daftar Anggota Resmi** (Database MySQL).
> - **Pengecekan Peran (Role Checking):**
>   - Jika nama terdaftar sebagai **Kasir**, Penjaga akan mengantarkannya ke **Ruang Kasir/POS** (`DashboardKasir`) untuk melayani transaksi.
>   - Jika nama terdaftar sebagai **Owner**, Penjaga akan mengantarkannya ke **Ruang Direksi** (`DashboardOwner`) untuk melihat laporan dan mengelola menu produk.
>   - Jika data tidak cocok, Penjaga akan menolak akses masuk dengan memberikan peringatan ("Username/Password Salah!").
> - **Disposing Form (`this.dispose()`):** Setelah pintu dibuka dan pengunjung masuk ke dalam gedung, gerbang luar ditutup kembali agar tidak menumpuk di depan.

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
                     +---------------------------------------+
                     |             INTERFACE (UI)            |
                     |  [tfUsername]      [tfPassword]       |
                     +-------+-----------------+-------------+
                             |                 |
                       (getText().trim())   (getText())
                             |                 |
                             v                 v
                     +---------------------------------------+
                     |        btnLoginActionPerformed        |
                     |  - Validasi kosong?                   |
                     |  - Siapkan SQL Parameter              |
                     +-------------------+-------------------+
                                         |
                             (Koneksi.getConnection())
                                         |
                                         v
                     +---------------------------------------+
                     |            DATABASE MYSQL             |
                     |  Query: SELECT * FROM user WHERE      |
                     |  username = ? AND password = ?        |
                     +-------------------+-------------------+
                                         |
                                (ResultSet rs)
                                         |
                                         v
                     +---------------------------------------+
                     |      PENGECEKAN HASIL (rs.next())     |
                     |                                       |
                     | [Baris Ditemukan?] --(Tidak)--> Gagal |
                     +---------+-----------------------------+
                               |
                             (Ya)
                               |
                               v
                     +---------------------------------------+
                     |            ROLE PENGGUNA              |
                     |                                       |
                     |   - Kasir -> Buka DashboardKasir      |
                     |   - Owner -> Buka DashboardOwner      |
                     |   - Akhir -> this.dispose()           |
                     +---------------------------------------+
```

---

## 3. Struktur Komponen Visual (GUI)
Form ini dibuat menggunakan Java Swing dengan komponen utama sebagai berikut:
1. `tfUsername` (`JTextField`): Tempat user mengetikkan username mereka.
2. `tfPassword` (`JTextField`): Tempat user mengetikkan password mereka.
3. `btnLogin` (`JButton`): Tombol untuk mengirimkan data login dan memulai pencocokan ke database.
4. `jLabel1` s.d `jLabel4` (`JLabel`): Label teks dekoratif untuk menampilkan judul aplikasi ("OriTeh Sapuro", "Sistem Informasi Penjualan") dan instruksi input.

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Deklarasi Package & Struktur Kelas
```java
package View;

public class LoginForm extends javax.swing.JFrame {
```
* **`package View;`** (Baris 1): Menentukan lokasi folder dari kelas ini. Kelas ini berada di dalam package `View` (Lapisan presentasi/antarmuka pengguna).
* **`public class LoginForm extends javax.swing.JFrame`** (Baris 3): Mendeklarasikan kelas `LoginForm` dengan akses `public` (bisa diakses dari package lain). Kelas ini mewarisi (`extends`) `javax.swing.JFrame`, yang berarti `LoginForm` adalah jendela aplikasi desktop (Windows Form).

---

### B. Konstruktor Kelas
```java
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginForm.class.getName());
    public LoginForm() {
        initComponents();
        this.setLocationRelativeTo(null);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
    }
```
* **`private static final java.util.logging.Logger logger...`** (Baris 5): Membuat logger statis untuk kelas ini. Digunakan untuk mencatat pesan error atau log sistem secara terstandar.
* **`public LoginForm()`** (Baris 6): Merupakan **Constructor** kelas. Fungsi ini dipanggil pertama kali saat objek `LoginForm` dibuat (`new LoginForm()`).
* **`initComponents();`** (Baris 7): Memanggil metode bawaan NetBeans GUI Builder untuk menginisialisasi properti visual komponen (ukuran tombol, jenis font, tata letak/layout).
* **`this.setLocationRelativeTo(null);`** (Baris 8): Mengatur posisi jendela agar muncul tepat di **tengah layar komputer** saat aplikasi dijalankan.
* **`btnLogin.addActionListener(...)`** (Baris 9-13): Mendaftarkan event listener pada tombol login. Ketika tombol `btnLogin` diklik oleh pengguna, Java akan mendeteksi aksi tersebut dan secara otomatis memicu metode `btnLoginActionPerformed(evt)`.

---

### C. Logika Utama Login (`btnLoginActionPerformed`)
```java
    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText();
```
* **`tfUsername.getText().trim()`** (Baris 17): Mengambil teks yang diketikkan di text field username, lalu memanggil `.trim()` untuk menghapus spasi kosong yang tidak sengaja terketik di awal maupun akhir teks.
* **`tfPassword.getText()`** (Baris 18): Mengambil teks asli dari input field password.

```java
        if (username.isEmpty() || password.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
```
* **`if (username.isEmpty() || password.isEmpty())`** (Baris 20): Validasi input. Jika kolom username ATAU password kosong:
  * **`JOptionPane.showMessageDialog(...)`** (Baris 21): Menampilkan kotak dialog peringatan (*Pop-Up*) dengan ikon Warning.
  * **`return;`** (Baris 22): Menghentikan eksekusi kode di dalam metode ini agar program tidak melanjutkan proses query ke database.

```java
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
```
* **`String query = ...`** (Baris 25): Menyiapkan query SQL untuk mencari user yang memiliki username dan password yang cocok. Karakter tanda tanya (`?`) disebut **Placeholder** (parameter) yang akan diisi kemudian untuk mencegah serangan keamanan berbahaya seperti **SQL Injection**.

```java
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
```
* **`try (...)`** (Baris 26-27): Menggunakan fitur **Try-With-Resources** Java. Koneksi database `conn` dan statement query `stmt` dideklarasikan di dalam kurung `try`. Fitur ini menjamin koneksi database akan ditutup secara otomatis setelah blok kode selesai dijalankan, mencegah kebocoran memori (memory leak).
* **`Models.Koneksi.getConnection()`**: Memanggil fungsi dari kelas helper koneksi untuk mendapatkan akses ke database MySQL.
* **`conn.prepareStatement(query)`**: Mempersiapkan statement query SQL yang aman.

```java
            stmt.setString(1, username);
            stmt.setString(2, password);
```
* **`stmt.setString(1, username)`** (Baris 28): Mengisi placeholder tanda tanya pertama (`?`) dengan nilai variabel `username`.
* **`stmt.setString(2, password)`** (Baris 29): Mengisi placeholder tanda tanya kedua (`?`) dengan nilai variabel `password`.

```java
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
```
* **`stmt.executeQuery()`** (Baris 30): Menjalankan query SQL ke database MySQL dan mengembalikan hasilnya dalam objek `ResultSet` bernama `rs`.
* **`if (rs.next())`** (Baris 31): Menggeser kursor data ke baris pertama hasil pencarian. Jika baris data ditemukan (kembalian bernilai `true`), berarti username dan password tersebut cocok dengan data di database.

```java
                    String role = rs.getString("role");
                    String nama = rs.getString("nama");
                    int idUser = rs.getInt("id_user");
                    
                    javax.swing.JOptionPane.showMessageDialog(this, "Login Berhasil! Selamat datang " + nama);
```
* **`rs.getString("role")`** dsb (Baris 32-34): Mengambil nilai kolom database `role`, `nama`, dan `id_user` dari akun yang berhasil login.
* **`JOptionPane.showMessageDialog(...)`** (Baris 36): Menampilkan pesan sukses login kepada pengguna.

```java
                    if (role.equalsIgnoreCase("Kasir")) {
                        DashboardKasir dk = new DashboardKasir();
                        dk.setCashierInfo(nama, idUser);
                        dk.setVisible(true);
```
* **`role.equalsIgnoreCase("Kasir")`** (Baris 38): Membandingkan isi string role dengan kata "Kasir" tanpa memedulikan perbedaan huruf besar dan kecil (case-insensitive).
* **`DashboardKasir dk = new DashboardKasir();`** (Baris 39): Membuat instance/objek baru dari halaman Dashboard Kasir.
* **`dk.setCashierInfo(nama, idUser);`** (Baris 40): Mengirimkan nama kasir dan id user ke dashboard kasir agar transaksi yang dicatat nantinya terasosiasi dengan kasir yang sedang bertugas.
* **`dk.setVisible(true);`** (Baris 41): Menampilkan jendela Dashboard Kasir ke layar.

```java
                    } else if (role.equalsIgnoreCase("Owner")) {
                        javax.swing.JFrame frame = new javax.swing.JFrame("OriTeh Sapuro - Owner Dashboard");
                        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                        DashboardOwner doPanel = new DashboardOwner();
                        frame.setContentPane(doPanel);
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
```
* **`role.equalsIgnoreCase("Owner")`** (Baris 42): Jika perannya adalah Owner:
  * **`new javax.swing.JFrame(...)`** (Baris 43): Membuat jendela frame baru.
  * **`frame.setDefaultCloseOperation(...)`** (Baris 44): Mengatur agar aplikasi berhenti sepenuhnya saat jendela dashboard owner ditutup.
  * **`DashboardOwner doPanel = new DashboardOwner();`** (Baris 45): Membuat objek panel `DashboardOwner` (karena `DashboardOwner` diturunkan dari `JPanel`).
  * **`frame.setContentPane(doPanel);`** (Baris 46): Memasang panel dashboard owner ke dalam frame utama.
  * **`frame.pack();`** (Baris 47): Menyesuaikan ukuran frame agar pas dengan ukuran komponen di dalamnya.
  * **`frame.setLocationRelativeTo(null);`** (Baris 48): Memposisikan dashboard owner di tengah layar.
  * **`frame.setVisible(true);`** (Baris 49): Menampilkan dashboard owner ke layar.

```java
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "Role tidak dikenal: " + role, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    this.dispose();
```
* **`this.dispose();`** (Baris 54): Menutup dan menghancurkan jendela `LoginForm` yang sedang aktif agar memori komputer dibebaskan kembali dan layar login hilang.

```java
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error database: " + ex.getMessage(), "Error Database", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
```
* **`else`** (Baris 55-57): Jika kursor `rs.next()` bernilai `false`, artinya database tidak menemukan kecocokan kombinasi username dan password. Tampilkan dialog gagal login.
* **`catch (java.sql.SQLException ex)`** (Baris 59-61): Jika terjadi gangguan koneksi database MySQL, jalankan penanganan error (*exception handling*) dengan menampilkan pesan kegagalan koneksi database.



---


---

## Source Code Lengkap (LoginForm.java)

Berikut adalah kode sumber lengkap dari file [LoginForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/LoginForm.java):

```java
package View;

public class LoginForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginForm.class.getName());
    public LoginForm() {
        initComponents();
        this.setLocationRelativeTo(null);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        String username = tfUsername.getText().trim();
        String password = tfPassword.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String nama = rs.getString("nama");
                    int idUser = rs.getInt("id_user");
                    
                    javax.swing.JOptionPane.showMessageDialog(this, "Login Berhasil! Selamat datang " + nama);
                    
                    if (role.equalsIgnoreCase("Kasir")) {
                        DashboardKasir dk = new DashboardKasir();
                        dk.setCashierInfo(nama, idUser);
                        dk.setVisible(true);
                    } else if (role.equalsIgnoreCase("Owner")) {
                        javax.swing.JFrame frame = new javax.swing.JFrame("OriTeh Sapuro - Owner Dashboard");
                        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                        DashboardOwner doPanel = new DashboardOwner();
                        frame.setContentPane(doPanel);
                        frame.pack();
                        frame.setLocationRelativeTo(null);
                        frame.setVisible(true);
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, "Role tidak dikenal: " + role, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    this.dispose();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error database: " + ex.getMessage(), "Error Database", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        tfUsername = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfPassword = new javax.swing.JTextField();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("OriTeh Sapuro - Pekalongan");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Sistem Informasi Penjualan");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Username :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Password :");

        btnLogin.setText("Login");
        btnLogin.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3)
                    .addComponent(tfUsername, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addComponent(tfPassword, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnLogin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(125, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(120, 120, 120))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(37, 37, 37)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(92, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField tfPassword;
    private javax.swing.JTextField tfUsername;
    // End of variables declaration//GEN-END:variables
}

```
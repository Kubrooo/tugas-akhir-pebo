# Penjelasan Rinci & Panduan Presentasi: `ProdukForm.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [ProdukForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/ProdukForm.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Papan Daftar Menu dan Kartu Resep Kedai**
>
> Bayangkan `ProdukForm` adalah sebuah **Papan Pengaturan Menu Minuman** yang terletak di dapur **OriTeh Sapuro**:
> - **Tambah Produk (`btnTambah`):** Membuat gantungan papan kayu menu baru dengan menuliskan Kode Unik (misal `OTH-JSM` untuk Jasmine Tea), Nama Teh, dan Harganya, lalu mencantelkannya di dinding daftar menu aktif.
> - **Edit Produk (`btnEdit`):** Menurunkan papan kayu menu yang sudah ada, lalu menulis ulang nama atau harganya yang baru dengan spidol. *Kode Produk dikunci tidak boleh diubah* (`tfKodeProduk.setEditable(false)`) karena itu adalah identitas paten papan kayu tersebut di database.
> - **Pencarian Dinamis (`tfSearchBar`):** Seperti asisten dapur yang dengan cepat memindai papan menu dengan matanya saat diteriaki kata kunci "Es" atau "Jasmine". Papan menu langsung memilah teh yang memiliki kata tersebut.
> - **Hapus Produk / Soft Delete (`btnHapus`):** Jika varian rasa teh tersebut tidak akan dijual lagi, Owner **tidak membakar papan menu lama** tersebut ke tempat sampah. Mengapa? Karena nota penjualan pelanggan bulan lalu yang berisi pesanan teh tersebut akan rusak laporannya jika identitas teh tersebut hilang total dari database. 
>   - Sebagai gantinya, Owner hanya menempelkan label stiker merah bertuliskan **"DIARSIPKAN/HABIS" (`is_deleted = 1`)** pada papan menu tersebut. Papan menu diturunkan dari pajangan depan agar tidak bisa dibeli lagi, namun fisiknya tetap disimpan di lemari arsip gudang (database).

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
  [ tfSearchBar (Ketik Huruf) ]        [ jTable1 (Klik Baris) ]
               |                                 |
        (keyReleased)                    (mouseClicked)
               |                                 |
               v                                 v
        [ loadData() ]                 Pindahkan isi kolom ke:
    - Query: SELECT * FROM produk      - tfKodeProduk (lock)
      WHERE is_deleted = 0             - tfNamaProduk
      AND (nama_produk LIKE ?)         - tfHargaProduk
               |
               +---------------+---------------+
                               |
                               v
                     [ KASUS TOMBOL AKSI ]
                               |
        +----------------------+----------------------+
        | (Tambah)             | (Edit)               | (Hapus)
        v                      v                      v
   [ btnTambah ]          [ btnEdit ]            [ btnHapus ]
   - Validasi input       - Ambil terpilih       - JOptionPane confirm
   - INSERT INTO produk   - UPDATE produk SET    - UPDATE produk SET
     (kode, nama, harga)    nama=?, harga=?        is_deleted = 1
                            WHERE kode=?         (Soft Delete Pattern)
        |                      |                      |
        +----------------------+----------------------+
                               |
                               v
                         [ loadData() ]
                     (Segarkan tabel GUI)
                               |
                               v
                        [ clearFields() ]
                    (Kosongkan kolom input)
```

---

## 3. Struktur Komponen Visual (GUI)
Form ini dirancang untuk operasi CRUD (Create, Read, Update, Delete) produk minuman:
1. `tfKodeProduk` (`JTextField`): Kolom input kode produk (Primary Key).
2. `tfNamaProduk` (`JTextField`): Kolom input nama menu teh.
3. `tfHargaProduk` (`JTextField`): Kolom input harga teh per gelas.
4. `tfSearchBar` (`JTextField`): Kolom pencarian dinamis menu produk.
5. `jTable1` (`JTable`): Tabel daftar visual menu produk aktif.
6. `btnTambah`, `btnEdit`, `btnHapus` (`JButton`): Tombol aksi untuk operasi data produk.

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Konstruktor Utama & Konfigurasi Awal
```java
    public ProdukForm() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
```
*   **`initComponents();`**: Inisialisasi bawaan Swing untuk merakit tampilan GUI menu manajemen produk.
*   **`this.setLocationRelativeTo(null);`**: Meletakkan jendela `ProdukForm` tepat di **tengah layar** saat dijalankan.
*   **`this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);`**: Mengatur agar jika tombol close `X` diklik, hanya jendela manajemen produk ini saja yang dihancurkan (`DISPOSE`) dari memori komputer tanpa mematikan program utama.

```java
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                javax.swing.JFrame frame = new javax.swing.JFrame("OriTeh Sapuro - Owner Dashboard");
                frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                DashboardOwner doPanel = new DashboardOwner();
                frame.setContentPane(doPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
```
*   **`addWindowListener`**: Mendaftarkan listener aktivitas jendela menggunakan *WindowAdapter*.
*   **`windowClosed` (Override)**: Dipicu otomatis sesaat setelah jendela produk ini tertutup penuh. Program akan membuat jendela `JFrame` baru bertuliskan *"OriTeh Sapuro - Owner Dashboard"*, menempelkan panel [DashboardOwner](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/DashboardOwner.java) di dalamnya, menyesuaikan ukuran (`pack()`), menaruhnya di tengah layar, dan menampilkannya agar owner otomatis kembali ke menu dashboard utama.

```java
        loadData();
        
        tfSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadData();
            }
        });
```
*   **`loadData();`**: Pemuatan tabel katalog produk secara otomatis saat form pertama kali dirender.
*   **`keyReleased` (Override)**: Event key listener pada kolom pencarian. Setiap kali jari pengguna **melepas tombol keyboard** saat mengetik di pencarian, method `loadData()` akan langsung dipanggil kembali secara dinamis untuk memfilter produk secara *real-time* huruf demi huruf.

---

### B. Pemuatan Data Katalog (`loadData`)
```java
    private void loadData() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "Kode Produk", "Nama Produk", "Harga Produk"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(model);
```
*   **`DefaultTableModel`**: Wadah model data tabel yang diisi tajuk kolom (No, Kode Produk, Nama Produk, Harga Produk).
*   **`isCellEditable` (Override)**: Dipaksa mengembalikan nilai `false` agar data di sel-sel tabel tidak dapat diedit secara langsung (Read-Only).

```java
        String search = tfSearchBar.getText().trim();
        String query = "SELECT * FROM produk WHERE is_deleted = 0";
        if (!search.isEmpty()) {
            query += " AND (kode_produk LIKE ? OR nama_produk LIKE ?)";
        }
        
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            if (!search.isEmpty()) {
                stmt.setString(1, "%" + search + "%");
                stmt.setString(2, "%" + search + "%");
            }
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        no++,
                        rs.getString("kode_produk"),
                        rs.getString("nama_produk"),
                        rs.getDouble("harga_produk")
                    });
                }
            }
        } catch (java.sql.SQLException ex) { ... }
```
*   **Formulasi Query Dinamis:** Jika kolom pencarian diisi (`!search.isEmpty()`), klausa pencarian `LIKE` ditambahkan ke query SQL.
*   **`stmt.setString(1, "%" + search + "%")` & `setString(2, ...)`**: Mengisi parameter `?` ke-1 dan ke-2 dengan pola string pencarian diapit karakter wildcard `%`.
*   **`stmt.executeQuery()`**: Mengirimkan perintah SELECT ke database MySQL.
*   **`rs.next()` & `model.addRow()`**: Perulangan kursor data untuk menarik nilai string (`rs.getString`) dan harga (`rs.getDouble`) guna dimasukkan sebagai baris-baris data visual ke dalam model tabel.

---

### C. Klik Baris Tabel Mengisi Kolom Input (`tableMouseClicked`)
```java
    private void tableMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            tfKodeProduk.setText(jTable1.getValueAt(selectedRow, 1).toString());
            tfNamaProduk.setText(jTable1.getValueAt(selectedRow, 2).toString());
            tfHargaProduk.setText(jTable1.getValueAt(selectedRow, 3).toString());
            tfKodeProduk.setEditable(false);
        }
    }
```
*   **`jTable1.getSelectedRow()`**: Mendapatkan nomor indeks baris tabel produk yang diklik oleh owner.
*   **`getValueAt(selectedRow, Kolom)`**: Mengambil data pada kolom tertentu (1: Kode, 2: Nama, 3: Harga) untuk disalin ke kolom-kolom input teks GUI.
*   **`tfKodeProduk.setEditable(false);`**: **Sangat Penting!** Menonaktifkan input pada text field Kode Produk. Karena Kode Produk bertindak sebagai Primary Key di database, mengubah kode produk di tengah jalan saat edit akan merusak integritas data relasional tabel.

---

### D. Operasi Tambah Data (Create)
```java
    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        // ... validasi kosong & parsing harga ...
        String query = "INSERT INTO produk (kode_produk, nama_produk, harga_produk) VALUES (?, ?, ?)";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, kode);
            stmt.setString(2, nama);
            stmt.setDouble(3, harga);
            stmt.executeUpdate();
            
            loadData();
            clearFields();
        } catch (java.sql.SQLException ex) { ... }
    }
```
*   **`Double.parseDouble(hargaStr)`**: Mengonversi teks harga masukan menjadi angka desimal (`double`). Jika gagal, sistem memunculkan dialog peringatan format tidak valid.
*   **`PreparedStatement` Mapping**:
    *   `1` -> mengisi `kode_produk` (`?` pertama) bertipe String.
    *   `2` -> mengisi `nama_produk` (`?` kedua) bertipe String.
    *   `3` -> mengisi `harga_produk` (`?` ketiga) bertipe Double.
*   **`stmt.executeUpdate()`**: Mengeksekusi penulisan query `INSERT` ke database. Jika berhasil, data dimuat ulang (`loadData()`) dan form dibersihkan (`clearFields()`).

---

### E. Operasi Ubah Data (Update)
```java
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {
        // ... validasi baris & input kosong ...
        String query = "UPDATE produk SET nama_produk = ?, harga_produk = ? WHERE kode_produk = ?";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nama);
            stmt.setDouble(2, harga);
            stmt.setString(3, kode);
            stmt.executeUpdate();
            
            loadData();
            clearFields();
        } catch (java.sql.SQLException ex) { ... }
    }
```
*   **`UPDATE` Query:** Memperbarui data produk berdasarkan kode produk yang ditargetkan.
*   **`PreparedStatement` Mapping**:
    *   `1` -> mengisi `nama_produk` (`?` pertama) bertipe String.
    *   `2` -> mengisi `harga_produk` (`?` kedua) bertipe Double.
    *   `3` -> mengisi `kode_produk` (`?` ketiga) bertipe String.

---

### F. Operasi Hapus Data (Delete - Soft Delete Pattern)
```java
    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        // ...
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus produk " + kode + "?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            String query = "UPDATE produk SET is_deleted = 1 WHERE kode_produk = ?";
            try (java.sql.Connection conn = Models.Koneksi.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, kode);
                stmt.executeUpdate();
                
                loadData();
                clearFields();
            } catch (java.sql.SQLException ex) { ... }
        }
    }
```
*   **`JOptionPane.showConfirmDialog(...)`**: Meminta konfirmasi Ya/Tidak kepada user untuk menghindari ketidaksengajaan terhapusnya data.
*   **`UPDATE produk SET is_deleted = 1`**: Mengimplementasikan pola **Soft Delete**. Kita tidak menghapus baris fisik produk dari database menggunakan perintah `DELETE FROM produk`. Hal ini dilakukan karena jika produk dihapus fisik secara permanen, maka riwayat transaksi belanja pelanggan yang merujuk pada produk tersebut di masa lalu akan mengalami error relasi (*foreign key constraint failure*). Sebagai gantinya, status `is_deleted` disetel ke `1` agar produk "tersembunyi" dari antarmuka penjualan saat ini, namun datanya tetap tersimpan utuh di database.

---

## 5. Pertanyaan Sidang yang Sering Ditanyakan (FAQ Sidang)

### Q: Mengapa Kode Produk dikunci (`setEditable(false)`) ketika baris tabel diklik?
*   **Jawaban:** Kode produk bertindak sebagai **Primary Key** (identitas unik) di database MySQL. Jika diperbolehkan mengubah kode produk saat melakukan proses edit, database tidak akan menemukan baris data asli yang ingin diupdate atau akan merusak integritas relasi foreign key pada riwayat transaksi.

### Q: Apa perbedaan penerapan query hapus dengan Hard Delete vs Soft Delete di program ini?
*   **Jawaban:** **Hard Delete** menggunakan query `DELETE FROM produk` yang langsung menghapus fisik data produk dari disk database, yang berdampak merusak histori laporan transaksi penjualan sebelumnya. Sedangkan program ini menerapkan **Soft Delete** lewat query `UPDATE produk SET is_deleted = 1` yang hanya menonaktifkan status produk agar tidak muncul di form kasir, tetapi riwayat transaksi masa lalu tetap dapat dirender dengan benar.

### Q: Apa fungsi simbol persen `%` pada syntax `stmt.setString(1, "%" + search + "%")`?
*   **Jawaban:** Di SQL, tanda persen `%` adalah karakter *wildcard* yang digunakan bersama operator `LIKE`. Ini berarti pencarian akan menemukan baris yang mengandung kata kunci tersebut di posisi mana sajaâ€”baik di awal, tengah, maupun di akhir teks produk.



---


---

## Source Code Lengkap (ProdukForm.java)

Berikut adalah kode sumber lengkap dari file [ProdukForm.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/ProdukForm.java):

```java
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

/**
 *
 * @author Ardiansyah
 */
public class ProdukForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProdukForm.class.getName());

    /**
     * Creates new form ProdukForm
     */
    public ProdukForm() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                javax.swing.JFrame frame = new javax.swing.JFrame("OriTeh Sapuro - Owner Dashboard");
                frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                DashboardOwner doPanel = new DashboardOwner();
                frame.setContentPane(doPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
        
        loadData();
        
        tfSearchBar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadData();
            }
        });
        
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });
        
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
    }

    private void loadData() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"No", "Kode Produk", "Nama Produk", "Harga Produk"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable1.setModel(model);
        
        String search = tfSearchBar.getText().trim();
        String query = "SELECT * FROM produk WHERE is_deleted = 0";
        if (!search.isEmpty()) {
            query += " AND (kode_produk LIKE ? OR nama_produk LIKE ?)";
        }
        
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            if (!search.isEmpty()) {
                stmt.setString(1, "%" + search + "%");
                stmt.setString(2, "%" + search + "%");
            }
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                int no = 1;
                while (rs.next()) {
                    model.addRow(new Object[]{
                        no++,
                        rs.getString("kode_produk"),
                        rs.getString("nama_produk"),
                        rs.getDouble("harga_produk")
                    });
                }
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error load data: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void tableMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            tfKodeProduk.setText(jTable1.getValueAt(selectedRow, 1).toString());
            tfNamaProduk.setText(jTable1.getValueAt(selectedRow, 2).toString());
            tfHargaProduk.setText(jTable1.getValueAt(selectedRow, 3).toString());
            tfKodeProduk.setEditable(false);
        }
    }
    
    private void clearFields() {
        tfKodeProduk.setText("");
        tfNamaProduk.setText("");
        tfHargaProduk.setText("");
        tfKodeProduk.setEditable(true);
        jTable1.clearSelection();
    }
    
    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        String kode = tfKodeProduk.getText().trim();
        String nama = tfNamaProduk.getText().trim();
        String hargaStr = tfHargaProduk.getText().trim();
        
        if (kode.isEmpty() || nama.isEmpty() || hargaStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Semua data harus diisi!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String query = "INSERT INTO produk (kode_produk, nama_produk, harga_produk) VALUES (?, ?, ?)";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, kode);
            stmt.setString(2, nama);
            stmt.setDouble(3, harga);
            stmt.executeUpdate();
            
            javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan!");
            loadData();
            clearFields();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error tambah produk: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk yang ingin diedit di tabel!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String kode = tfKodeProduk.getText().trim();
        String nama = tfNamaProduk.getText().trim();
        String hargaStr = tfHargaProduk.getText().trim();
        
        if (nama.isEmpty() || hargaStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Nama dan Harga tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Harga harus berupa angka!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String query = "UPDATE produk SET nama_produk = ?, harga_produk = ? WHERE kode_produk = ?";
        try (java.sql.Connection conn = Models.Koneksi.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nama);
            stmt.setDouble(2, harga);
            stmt.setString(3, kode);
            stmt.executeUpdate();
            
            javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil diperbarui!");
            loadData();
            clearFields();
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error edit produk: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pilih produk yang ingin dihapus di tabel!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String kode = tfKodeProduk.getText().trim();
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus produk " + kode + "?", "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
        
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            String query = "UPDATE produk SET is_deleted = 1 WHERE kode_produk = ?";
            try (java.sql.Connection conn = Models.Koneksi.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, kode);
                stmt.executeUpdate();
                
                javax.swing.JOptionPane.showMessageDialog(this, "Produk berhasil dihapus!");
                loadData();
                clearFields();
            } catch (java.sql.SQLException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error hapus produk: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
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

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        tfSearchBar = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        tfKodeProduk = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfNamaProduk = new javax.swing.JTextField();
        tfHargaProduk = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        btnTambah = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Manajemen Produk - OriTeh Sapuro");
        jLabel1.setToolTipText("");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(179, 179, 179))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Daftar Menu :");

        tfSearchBar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Search :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Kode Produk :");

        tfKodeProduk.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Nama Produk :");

        tfNamaProduk.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        tfHargaProduk.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Harga Produk :");

        btnTambah.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnTambah.setText("Tambah");

        btnEdit.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnEdit.setText("Edit");

        btnHapus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnHapus.setText("Hapus");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tfSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfKodeProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(btnTambah)
                        .addGap(18, 18, 18)
                        .addComponent(btnEdit)
                        .addGap(18, 18, 18)
                        .addComponent(btnHapus))
                    .addComponent(tfHargaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(tfNamaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel1)
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfSearchBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(5, 5, 5)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfKodeProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTambah)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfNamaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfHargaProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(29, Short.MAX_VALUE))
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
        java.awt.EventQueue.invokeLater(() -> new ProdukForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnTambah;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField tfHargaProduk;
    private javax.swing.JTextField tfKodeProduk;
    private javax.swing.JTextField tfNamaProduk;
    private javax.swing.JTextField tfSearchBar;
    // End of variables declaration//GEN-END:variables
}

```
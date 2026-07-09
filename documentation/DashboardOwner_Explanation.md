# Penjelasan Kode: `DashboardOwner.java`

Dokumen ini menjelaskan secara rinci kode sumber pada file [DashboardOwner.java](file:///c:/Users/Ardiansyah/Documents/NetBeansProjects/tugasAkhirPebo/src/main/java/View/DashboardOwner.java) untuk mempermudah pemahaman saat presentasi sidang atau tugas akhir.

---

## 1. Analogi Dunia Nyata (Real-World Analogy)
> [!NOTE]
> **Analogi: Meja Resepsionis Utama di Ruang Direksi (Owner)**
>
> Bayangkan `DashboardOwner` adalah sebuah **Meja Resepsionis Utama** di dalam Ruang Direksi Owner **OriTeh Sapuro**. Meja ini menyediakan beberapa tombol pintasan (Menu Utama) yang membawa Owner ke departemen-departemen khusus:
> - **Tombol Manajemen Produk (`btnManajemenProduk`):** Membuka pintu ke **Ruang Inventaris Produk** (`ProdukForm`) untuk menambah rasa teh baru atau mengubah harga. Ketika Owner masuk ke ruang tersebut, pintu ruang lobi ditutup (`win.dispose()`) agar tidak bingung.
> - **Tombol Laporan Penjualan Harian (`btnLaporanPenjualan1`):** Membuka laci **Buku Harian Penjualan** (`LaporanPenjualanHarianForm`) untuk memeriksa berapa gelas teh yang terjual hari ini.
> - **Tombol Laporan Penjualan Bulanan (`btnLaporanPenjualan`):** Membuka laci **Buku Rekapitulasi Bulanan** (`LaporanPenjualanBulananForm`) untuk melihat omzet bulanan.
> - **Tombol Logout (`btnLogout`):** Mengunci seluruh akses ruangan Owner dan mengembalikan Owner ke **Gerbang Keamanan Depan** (`LoginForm`).

---

## 2. Visualisasi Aliran Data (ASCII Data Flow)

```text
           +---------------------------------------------+
           |               DASHBOARD OWNER               |
           |             (Tampilan JPanel)               |
           +---+-------------------+---------------+-----+
               |                   |               |
        (Klik Tombol)       (Klik Tombol)     (Klik Tombol)
               |                   |               |
               v                   v               v
      [Manajemen Produk]    [Laporan Harian/    [Logout]
        (ProdukForm)            Bulanan)      (LoginForm)
               |                   |               |
               |             (Buka Form)           |
               +-------------------+---------------+
                                   |
                                   v
             +---------------------+---------------------+
             |         PROSES PERPINDAHAN WINDOW         |
             |                                           |
             |  1. Buka Jendela Form Target              |
             |     - targetForm.setVisible(true)         |
             |                                           |
             |  2. Cari Window Induk (JFrame)            |
             |     - SwingUtilities.getWindowAncestor()  |
             |                                           |
             |  3. Tutup Jendela Dashboard               |
             |     - win.dispose()                       |
             +-------------------------------------------+
```

---

## 3. Struktur Komponen Visual (GUI)
Form ini dirancang sebagai sebuah **`JPanel`** (Panel konten), bukan `JFrame` mandiri. Komponen utamanya adalah:
1. `btnManajemenProduk` (`JButton`): Tombol untuk membuka menu pengelolaan barang/produk.
2. `btnLaporanPenjualan1` (`JButton`): Tombol untuk membuka laporan harian.
3. `btnLaporanPenjualan` (`JButton`): Tombol untuk membuka laporan bulanan.
4. `btnLogout` (`JButton`): Tombol untuk keluar dari sesi akun Owner dan kembali ke login form.
5. `jLabel1` (`JLabel`): Label judul dashboard.

---

## 4. Penjelasan Alur Kode Baris demi Baris

### A. Deklarasi Package & Struktur Kelas
```java
package View;

public class DashboardOwner extends javax.swing.JPanel {
```
* **`package View;`** (Baris 5): Menandakan berkas ini berada di dalam package `View`.
* **`public class DashboardOwner extends javax.swing.JPanel`** (Baris 11): Mendeklarasikan kelas `DashboardOwner`. Berbeda dari `LoginForm` yang mewarisi `JFrame` (jendela mandiri), kelas ini mewarisi **`javax.swing.JPanel`** (sebuah panel kontainer/kanvas komponen yang harus ditempelkan di dalam sebuah `JFrame` agar bisa terlihat di layar).

---

### B. Konstruktor & Event Handling Tombol
```java
    public DashboardOwner() {
        initComponents();
```
* **`public DashboardOwner()`** (Baris 16): Constructor untuk menyiapkan dan merakit komponen panel.
* **`initComponents();`** (Baris 17): Fungsi untuk menyusun tata letak komponen GUI yang digenerate oleh editor NetBeans.

#### 1) Tombol Manajemen Produk
```java
        btnManajemenProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ProdukForm pf = new ProdukForm();
                pf.setVisible(true);
                
                java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(DashboardOwner.this);
                if (win != null) {
                    win.dispose();
                }
            }
        });
```
* **`btnManajemenProduk.addActionListener(...)`** (Baris 18): Menambahkan pendengar klik (*click listener*) ke tombol Manajemen Produk.
* **`ProdukForm pf = new ProdukForm(); pf.setVisible(true);`** (Baris 20-21): Membuat jendela halaman data produk dan menampilkannya di layar.
* **`javax.swing.SwingUtilities.getWindowAncestor(DashboardOwner.this)`** (Baris 24): Karena `DashboardOwner` adalah `JPanel` (bukan window), ia tidak bisa ditutup secara langsung. Fungsi utilitas ini digunakan untuk mencari **Window Induk/Leluhur** (dalam hal ini `JFrame` pembungkus panel) yang menampung panel ini.
* **`win.dispose();`** (Baris 26): Jika window induk ditemukan, tutup/hancurkan window induk tersebut untuk menghemat memori komputer.

#### 2) Tombol Laporan Penjualan Harian
```java
        btnLaporanPenjualan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LaporanPenjualanHarianForm lpf = new LaporanPenjualanHarianForm();
                lpf.setVisible(true);
                java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(DashboardOwner.this);
                if (win != null) {
                    win.dispose();
                }
            }
        });
```
* **`LaporanPenjualanHarianForm lpf = ...`** (Baris 32): Membuat objek form laporan harian dan menampilkannya.
* **`win.dispose();`** (Baris 36): Menutup halaman dashboard Owner agar fokus layar berpindah sepenuhnya ke Laporan Penjualan Harian.

#### 3) Tombol Laporan Penjualan Bulanan
```java
        btnLaporanPenjualan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LaporanPenjualanBulananForm lpf = new LaporanPenjualanBulananForm();
                lpf.setVisible(true);
                java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(DashboardOwner.this);
                if (win != null) {
                    win.dispose();
                }
            }
        });
```
* **`LaporanPenjualanBulananForm lpf = ...`** (Baris 42): Membuat objek form laporan bulanan dan menampilkannya di layar.
* **`win.dispose();`** (Baris 46): Menutup jendela dashboard Owner.

#### 4) Tombol Logout
```java
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(DashboardOwner.this);
                if (win != null) {
                    win.dispose();
                }
                new LoginForm().setVisible(true);
            }
        });
    }
```
* **`new LoginForm().setVisible(true)`** (Baris 56): Membuka kembali halaman login utama saat Owner memutuskan keluar.
* **`win.dispose()`** (Baris 54): Menutup dashboard Owner agar tidak bisa diakses tanpa login kembali.

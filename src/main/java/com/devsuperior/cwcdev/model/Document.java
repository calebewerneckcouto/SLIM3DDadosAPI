package com.devsuperior.cwcdev.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    
    @Column(name = "original_file_name")
    private String originalFileName;
    
    @Column(name = "file_type")
    private String fileType;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] fileData; // Armazenamento de dados binários do arquivo

    private Boolean shared;
    
    @ManyToOne(fetch = FetchType.LAZY)  // Relacionamento com Usuario
    @JoinColumn(name = "usuario_id")   // Coluna para o ID do usuário
    private Usuario usuario;

    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}

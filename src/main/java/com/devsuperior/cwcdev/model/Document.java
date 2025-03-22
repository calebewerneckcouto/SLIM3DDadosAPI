package com.devsuperior.cwcdev.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ElementCollection
    @CollectionTable(
        name = "document_shared_with_user_ids",
        joinColumns = @JoinColumn(name = "document_id")
    )
    @Column(name = "shared_with_user_ids")
    private List<Long> sharedWithUserIds;

    // Construtores
    public Document() {
    }

    public Document(Long id, String name, String description, String originalFileName, String fileType, byte[] fileData, Usuario usuario, List<Long> sharedWithUserIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileData = fileData;
        this.usuario = usuario;
        this.sharedWithUserIds = sharedWithUserIds;
    }

    // Getters e Setters
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

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Long> getSharedWithUserIds() {
        return sharedWithUserIds;
    }

    public void setSharedWithUserIds(List<Long> sharedWithUserIds) {
        this.sharedWithUserIds = sharedWithUserIds;
    }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ToString (opcional, para facilitar a depuração)
    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", usuario=" + usuario +
                ", sharedWithUserIds=" + sharedWithUserIds +
                '}';
    }
}
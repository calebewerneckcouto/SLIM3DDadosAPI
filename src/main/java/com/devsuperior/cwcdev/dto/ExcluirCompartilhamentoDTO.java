package com.devsuperior.cwcdev.dto;

public class ExcluirCompartilhamentoDTO {
    private Long documentoId;
    private Long usuarioId;

    // Getters e setters
    public Long getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(Long documentoId) {
        this.documentoId = documentoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}
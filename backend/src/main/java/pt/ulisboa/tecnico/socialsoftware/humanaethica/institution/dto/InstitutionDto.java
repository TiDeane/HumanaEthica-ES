package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;

public class InstitutionDto {
    private Integer id;

    private String email;

    private String name;

    private String nif;

    public InstitutionDto(){
    }

    public InstitutionDto(Institution institution){
        setId(institution.getId());
        setEmail(institution.getEmail());
        setName(institution.getName());
        setNif(institution.getNIF());
    }

    public InstitutionDto(RegisterInstitutionDto registerInstitutionDto){
        setEmail(registerInstitutionDto.getInstitutionEmail());
        setName(registerInstitutionDto.getInstitutionName());
        setNif(registerInstitutionDto.getInstitutionNif());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }
}

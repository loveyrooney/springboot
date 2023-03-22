package sesac.JPA.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
public class BoardDTO {

    @Id
    @GeneratedValue
    private int boardId;
    private String userId;
    private String title;
    private String content;
    private String date;
}
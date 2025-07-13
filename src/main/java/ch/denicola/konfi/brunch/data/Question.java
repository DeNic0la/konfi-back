package ch.denicola.konfi.brunch.data;

import jakarta.persistence.*;
import lombok.*;

import java.net.URL;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = Question.TABLE_NAME)
public class Question {
    public static final String TABLE_NAME = "question";
    public static final String COLUMN_ID_NAME = "id";
    public static final String COLUMN_TITLE_NAME = "title";
    public static final String COLUMN_LINK_NAME = "link";
    public static final String COLUMN_MIN_NAME = "min";
    public static final String COLUMN_MAX_NAME = "max";
    public static final String COLUMN_RECOMMENDED_NAME = "recommended";
    public static final String COLUMN_ORDER_NAME = "order";
    public static final String COLUMN_OPTIONAL_NAME = "optional";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID_NAME, nullable = false)
    private Integer id;

    @Column(name = COLUMN_TITLE_NAME)
    private String title;

    @Column(name = COLUMN_LINK_NAME)
    private URL link;

    @Column(name = COLUMN_MIN_NAME,columnDefinition = "integer default 0")
    private Integer min;

    @Column(name = COLUMN_MAX_NAME,columnDefinition = "integer default 0")
    private Integer max;

    @Column(name = COLUMN_RECOMMENDED_NAME)
    private Integer recommended;

    @Column(name = COLUMN_ORDER_NAME)
    private Integer order;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "brunch_id", nullable = false)
    private Brunch brunch;

    @Column(name = COLUMN_OPTIONAL_NAME,columnDefinition = "boolean default false")
    private Boolean optional;

}
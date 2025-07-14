package ch.denicola.konfi.brunch.data;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = Vote.TABLE_NAME)
public class Vote {
    public static final String TABLE_NAME = "vote";
    public static final String COLUMN_ID_NAME = "id";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_EMAIL_NAME = "email";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = COLUMN_ID_NAME, nullable = false)
    private Integer id;

    @Column(name = COLUMN_EMAIL_NAME)
    private String email;

    @Column(name = COLUMN_NAME_NAME)
    private String name;

    @ToString.Exclude
    @OneToMany(mappedBy = "vote", orphanRemoval = true)
    private List<VoteAnswer> voteAnswers = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.REMOVE, optional = false)
    @JoinColumn(name = "brunch_id", nullable = false)
    private Brunch brunch;

}
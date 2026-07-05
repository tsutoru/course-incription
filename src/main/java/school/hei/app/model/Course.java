package school.hei.app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String title;

  private String description;

  private double price;

  @Builder.Default
  @OneToMany(mappedBy = "course")
  private List<Subscription> subscriptions = new ArrayList<>();
}

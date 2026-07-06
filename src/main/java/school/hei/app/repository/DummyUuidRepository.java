package school.hei.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.app.PojaGenerated;
import school.hei.app.repository.model.DummyUuid;

@PojaGenerated
@Repository
public interface DummyUuidRepository extends JpaRepository<DummyUuid, String> {
  @Override
  List<DummyUuid> findAllById(Iterable<String> ids);
}

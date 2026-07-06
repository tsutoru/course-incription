package school.hei.app.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.app.PojaGenerated;
import school.hei.app.endpoint.event.model.UuidCreated;
import school.hei.app.repository.DummyUuidRepository;
import school.hei.app.repository.model.DummyUuid;

@PojaGenerated
@Service
@AllArgsConstructor
@Slf4j
public class UuidCreatedService implements Consumer<UuidCreated> {

  private final DummyUuidRepository dummyUuidRepository;

  @Override
  public void accept(UuidCreated uuidCreated) {
    var dummyUuid = new DummyUuid();
    dummyUuid.setId(uuidCreated.getUuid());
    dummyUuidRepository.save(dummyUuid);
  }
}

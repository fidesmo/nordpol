package nordpol;

import java.io.IOException;

public interface CardErrorListener {
    void error(IsoCard card, IOException exeption);
}

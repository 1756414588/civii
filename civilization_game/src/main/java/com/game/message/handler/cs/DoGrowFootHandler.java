package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoGrowFootRq;
import com.game.service.ActivityService;

public class DoGrowFootHandler extends ClientHandler {
	@Override
	public void action() {
		DoGrowFootRq req = msg.getExtension(DoGrowFootRq.ext);
		ActivityService service = getService(ActivityService.class);
		service.doGrowFootRq(req, this);
	}
}
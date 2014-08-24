//s = Server(\myServer, NetAddr("127.0.0.1", 44556));
s.options.memSize = 650000;
s.boot;
s.scope;

s.doWhenBooted({

	~len=180;
	~maketimer = {
		arg len=180;
		var timer;
		timer = ();
		timer.bus = Bus.control;
		timer.line = {Out.kr(timer.bus, Line.kr(0,1,len))}.play;
		timer;
	};

	~timer = ~maketimer.(len: 180);
	s.sync;
	~timer.bus.get;
	s.sync;


	SynthDef("ThreeOsc",
		{
			arg out=0,amp=0.3,r=0,g=0,b=0,gate=1;
			//var ee = EnvGen.kr( Env.adsr( 0.1, 0.3, 0.5, 1), gate, doneAction: 2);
			var ee = EnvGen.kr(Env.linen(0.3, 0.1, 0.1), doneAction: 2);
			Out.ar(out,
				Mix.ar([
					SinOsc.ar(30+r/255*1400,mul:amp/3.0),
					Saw.ar(30+g/255*1400,mul:amp/3.0),
					Pulse.ar(30+b/255*1400,mul:amp/3.0)
				]) * ee!2
			);
		}
	).load(s);
	s.sync;

	SynthDef("ThreeOscLong",
		{
			arg out=0,amp=0.3,r=0,g=0,b=0,gate=0,mix=0,room=0;
			//var ee = EnvGen.kr( Env.adsr( 0.1, 0.3, 0.5, 1), Dust.ar(20));
			//var ee = EnvGen.kr(Env.linen(0.3, 0.5, 0.5), gate);//+Dust.ar(1));//gate, doneAction: 0);
			var sinm, sawm, pulsem;
			sinm = SinOsc.kr(0.1+0.3*r,mul:255,phase:0.3);
			sawm = SinOsc.kr(0.1+0.6*g,mul:255,phase:0.1);
			pulsem = SinOsc.kr(0.1+0.9*b,mul:255,phase:0.9);
			Out.ar(out,
				FreeVerb.ar(
					//ee *
					Mix.ar([
						SinOsc.ar(255+20+sinm,mul:amp/3.0),
						Saw.ar(255+20+sawm,mul:amp/3.0),
						Pulse.ar(255+20+pulsem,mul:amp/3.0)
					])
					, mix: mix
					, room: room
				)!2
			);
		}
	).load(s);
	s.sync;

	~threelong = Synth.new("ThreeOscLong");
	~threelong.map(\mix, ~timer.bus);
	~threelong.map(\room, ~timer.bus);
	~timer.bus.get;

	~colour = { |msg|
		//msg[0].pushln;
		~threelong.set(\r,msg[1]);
		~threelong.set(\g,msg[2]);
		~threelong.set(\b,msg[3]);
		//~threelong.set(\gate, 1);
		//~threelong.set(\gate, 0);
		//~threelong.set(\r,msg[3]);
		//Synth.new("ThreeOsc",[\r,msg[4],\g,msg[5],\b,msg[6],\amp,0.1]);
		//Synth.new("ThreeOsc",[\r,msg[5],\g,msg[6],\b,msg[7],\amp,0.1]);
	};


	/* pop osc responder */
	o = ();
	o.n = NetAddr("127.0.0.1", 57120);
	o.o = OSCresponderNode(n, '/chat', { |t, r, msg| ("time:" + t).postln; msg[1].postln }).add;
	o.m = NetAddr("127.0.0.1", 57120); // the url should be the one of computer of app 1
	s.sync;
	o.m.sendMsg("/chat", "Hello App 1");
	o.m.sendBundle(2.0, ["/chat", "Hello App 1"], ["/chat", "Hallo Wurld"]);
	o.m.sendBundle(0.0, ["/chat", "Hello App 1"], ["/chat", "Hallo Wurld"]);
	o.pops = OSCresponderNode(n, '/colour',
		{ arg t, r, msg;
			msg.postln;
			~colour.(msg);
		}
	).add;
	s.sync;

	o.m.sendBundle(0.0, ["/colour",255,255,255,0,0,10,10]);
	o.m.sendBundle(0.0, ["/colour",127,128,129,200,200,201,201]);

	s.sync;
});
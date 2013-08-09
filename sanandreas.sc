//s = Server(\myServer, NetAddr("127.0.0.1", 44556)); 
s.options.memSize = 650000;
s.boot;
s.scope;

SynthDef("ThreeOsc",
	{ 
		arg out=0,amp=0.3,r=0,g=0,b=0,gate=1;
		//var ee = EnvGen.kr( Env.adsr( 0.1, 0.3, 0.5, 1), gate, doneAction: 2);		
		var ee = EnvGen.kr(Env.linen(0.3, 0.1, 0.1), doneAction: 2);
		Out.ar(out,
			Mix.ar([
				SinOsc.ar(30+r/255*100,mul:amp/3.0),
				Saw.ar(30+g/255*100,mul:amp/3.0),
				Pulse.ar(30+b/255*100,mul:amp/3.0)
			]) * ee
		);
	}
).load(s)

SynthDef("ThreeOscLong",
	{ 
		arg out=0,amp=0.3,r=0,g=0,b=0,gate=1;
		//var ee = EnvGen.kr( Env.adsr( 0.1, 0.3, 0.5, 1), gate, doneAction: 2);		
		//var ee = EnvGen.kr(Env.linen(0.3, 0.1, 0.1), doneAction: 2);
		Out.ar(out,
			Mix.ar([
				SinOsc.ar(30+(r/255*1000)%80,mul:amp/3.0),
				Saw.ar(30+g/255*100,mul:amp/3.0),
				Pulse.ar(30+b/255*100,mul:amp/3.0)
			])
		);
	}
).load(s);

~threelong = Synth.new("ThreeOscLong");

~colour = { |msg|
	~threelong.set(\r,msg[0]);
	~threelong.set(\g,msg[1]);
	~threelong.set(\b,msg[2]);
	~threelong.set(\r,msg[3]);

	//Synth.new("ThreeOsc",[\r,msg[0],\g,msg[1],\b,msg[2]]);
};


//{ Mix.ar([				SinOsc.ar(30),Saw.ar(30),Pulse.ar(30)			]) }.play;
//~k = Synth.new("ThreeOsc");
//~k.set(\gate,0)
//Synth.new("ThreeOsc",[\r,55,\g,25,\b,55]);

//Synth.new("ThreeOsc",[\r,128,\g,128,\b,128]);

/*
~colour = { |msg|
	Synth.new("ThreeOsc",[\r,msg[0],\g,msg[1],\b,msg[2]]);
};
*/

/* pop osc responder */
o = ();
o.n = NetAddr("127.0.0.1", 57120); 
o.o = OSCresponderNode(n, '/chat', { |t, r, msg| ("time:" + t).postln; msg[1].postln }).add;
o.m = NetAddr("127.0.0.1", 57120); // the url should be the one of computer of app 1
o.m.sendMsg("/chat", "Hello App 1");
o.m.sendBundle(2.0, ["/chat", "Hello App 1"], ["/chat", "Hallo Wurld"]);
o.m.sendBundle(0.0, ["/chat", "Hello App 1"], ["/chat", "Hallo Wurld"]);
o.pops = OSCresponderNode(n, '/colour', 
	{ arg t, r, msg;
		msg.postln;
		~colour.(msg);
	}
).add;

o.m.sendBundle(0.0, ["/colour",255,255,255,0,0,10,10]);
o.m.sendBundle(0.0, ["/colour",127,128,129,200,200,201,201]);

/* o.m.remove; */

/*

o.scratch.remove;
o.crinkle.remove;
o.pops.remove;


*/





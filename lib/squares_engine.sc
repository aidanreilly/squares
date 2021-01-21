// a few squares
// https://github.com/catfact/zebra/blob/master/lib/Engine_Zsins.sc
// thank you zebra

Engine_Squares : CroneEngine {
  classvar num;
  var <synth;

  *initClass {  num = 16; }

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {
    var server = Crone.server;
    var def = SynthDef.new(\sin, {
      arg out, vol=0.0, hz=220, hz_lag=0.005,
        env_bias=0.0, amp_atk=0.001, amp_rel=0.05,
        pan=0.0, pan_lag=0.005, mul=1, filt_cutoff=440, filt_gain=0.95, pw=1.0, sample_rate=48000, bit_depth=24;
      var pulse1, pulse2, mix, filt, filt_decimate, amp_, hz_, pan_;
      amp_ = EnvGen.ar(Env.circle([0, 1, 0], [amp_atk, amp_rel, 0.001]), levelBias: env_bias);
      hz_ = Lag.ar(K2A.ar(hz), hz_lag);
      pan_ = Lag.ar(K2A.ar(pan), pan_lag);
      pulse1 = Pulse.ar(hz_ + (0.01 * LFNoise1.kr(5.reciprocal).abs), pw, mul);
      pulse2 = Pulse.ar(hz_ + (0.02 * LFNoise1.kr(6.reciprocal).abs), pw, mul);
      mix = Mix.new(pulse1, pulse2);
      filt = MoogFF.ar(mix, filt_cutoff, filt_gain);
      filt_decimate = Decimator.ar(filt, sample_rate, bit_depth, 1.0, 0);
      Out.ar(out, Pan2.ar(filt_decimate * amp_ * vol, pan_));
    });
    def.send(server);
    server.sync;

    synth = Array.fill(num, { Synth.new(\sin, [\out, context.out_b], target: context.xg) });

    #[\hz, \vol, \env_bias, \pan, \amp_atk, \amp_rel, \hz_lag, \pan_lag, \pw, \filt_cutoff, \filt_q, \filt_gain].do({
      arg name;
      this.addCommand(name, "if", {
        arg msg;
        var i = msg[1];
        synth[i].set(name, msg[2]);
      });
    });

    #[\sample_rate, \bit_depth].do({
      arg name;
      this.addCommand(name, "ii", {
        arg msg;
        var i = msg[1];
        synth[i].set(name, msg[2]);
      });
    });

  }

  free {
    synth.do({ |syn| syn.free; });
  }
}



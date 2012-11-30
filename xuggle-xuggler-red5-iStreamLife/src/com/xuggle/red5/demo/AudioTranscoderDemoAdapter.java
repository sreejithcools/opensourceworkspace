/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Xuggler-Red5.
 *
 * Xuggle-Xuggler-Red5 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Red5 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Red5.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.xuggle.red5.demo;

import java.util.Timer;
import java.util.TimerTask;

import org.red5.server.api.Red5;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.stream.IBroadcastStream;
import org.slf4j.Logger;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.renderer.SimpleTextRenderer;
import etm.core.timer.Java15NanoTimer;

/**
 * A demo red five application that takes a stream being broadcasted and
 * re-encodes it to 5.5khz stero PCM 16 bit audio.
 */
public class AudioTranscoderDemoAdapter extends MultiThreadedApplicationAdapter
{
  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  private AudioTranscoderDemo resamplerDemo = new AudioTranscoderDemo("xuggle_");
  private EtmMonitor profiler = EtmManager.getEtmMonitor();
  private Timer profilerRenderer = new Timer("profilerRenderer", true);
  private int mProfilerFrequency=0;

  public void setProfilerFrequency(int seconds)
  {
    mProfilerFrequency = seconds;
  }
  public void init()
  {
    log.debug("Demo has started.  Be afraid: {}", this.getClass().getName());
    BasicEtmConfigurator.configure(true, new Java15NanoTimer());
    profiler.start();
    
    // very simple timer here that spits out profiling data every 5 seconds
    if (mProfilerFrequency > 0)
    {
      profilerRenderer.schedule(new TimerTask(){
        @Override
        public void run()
        {
          System.out.println("Printing Statistics for: " + this.getClass().getName());
          profiler.render(new SimpleTextRenderer());
        }
      },
      mProfilerFrequency*1000,
      mProfilerFrequency*1000);
    }
  }
  
  /**
   * Called on publish: NetStream.publish("streamname", "live")
   */
  @Override
  public void streamPublishStart(IBroadcastStream stream) {
    log.debug("streamPublishStart: {}; {}", stream, stream.getPublishedName());
    super.streamPublishStart(stream);
    resamplerDemo.startTranscodingStream(stream, Red5.getConnectionLocal().getScope());
  }
  
  @Override
  public void streamBroadcastClose(IBroadcastStream stream) {
    log.debug("streamBroadcastClose: {}; {}", stream, stream.getPublishedName());
    
    resamplerDemo.stopTranscodingStream(stream, Red5.getConnectionLocal().getScope());
    super.streamBroadcastClose(stream);
  }



}

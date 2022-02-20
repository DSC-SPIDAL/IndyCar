import os

os.system("pip install iuindycar==0.0.8")

import iuindycar.Orchestrator as iui

#put token in the quotes
token="""TOKEN"""

oc = iui.Orchestrator(iui.Config(token))
cell =  iui.DetectionCell(storm_jar="Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar",
        class_name="com.dsc.iu.streaming.LSTMAnomalyDetectionTask")
oc.deploy_stream("indycar-22",
                 "201722",
                 "compact_topology_out",
                 detection_cell=cell)

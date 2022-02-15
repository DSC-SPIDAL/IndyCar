!pip install iuindycar==0.0.8

import iuindycar.Orchestrator as iui

#put token in the quotes
token="""eyJhbGciOiJSUzI1NiIsImtpZCI6IjRJSVdsOWNuWU42V09WQWRTcXN0bkFPX1A5WTA2cm91NC1zQkEyQXJ1SWMifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLWdoYnpuIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiJjMGRjOTM4Yy02ZjFjLTQxNTMtODU3NC1lMDAxYTZiNzA3MzUiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZXJuZXRlcy1kYXNoYm9hcmQ6YWRtaW4tdXNlciJ9.jeAzya1lZ7weYaffmPrILHJ-cmbBZgqTauWRu9qLyiRVMAgN-CsLnslCdfbJmB-Oec9ksWjbR81ffaqjQaEO_Gsftx4cPcnuXwURm5XkAFVeC8uKIfqb5NCAaP2AC9zkORW5JQxBOqSqTqtg1o9vA8YB-Ve1eRml74dnKsfjMalefvRQt4NngqFYO8MeFhePQraWTdK4Hm-K9jXZKbUaz5OuqRFhCJSInh46GpJcjTdsiSa5SqwFGi4ijpSpAUsp6bOwI6XYGpoW4MkE1YRvDFAI9_IgZSQwxDQe_QNNHs3qE1tSqk3H8N7lo6b2e7RpCjGxjiST4bEO1hECbq7Stw"""

oc = iui.Orchestrator(iui.Config(token))
cell =  iui.DetectionCell(storm_jar="Indycar500-33-HTMBaseline-1.0-SNAPSHOT.jar",
        class_name="com.dsc.iu.streaming.LSTMAnomalyDetectionTask")
oc.deploy_stream("indycar-22",
                 "201722",
                 "compact_topology_out",
                 detection_cell=cell)

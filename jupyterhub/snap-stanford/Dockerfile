FROM jupyter/scipy-notebook:latest

USER root

RUN apt-get update && \
    apt-get install -yq --no-install-recommends gnuplot graphviz  && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN wget http://snap.stanford.edu/snappy/release/snap-stanford-5.0.0-5.0-ubuntu18.04.2-x64-py3.6.tar.gz && \ 
    tar zxvf snap-stanford-5.0.0-5.0-ubuntu18.04.2-x64-py3.6.tar.gz && \
    cd snap-stanford-5.0.0-5.0-ubuntu18.04.2-x64-py3.6 && \
    python3 setup.py install && \
    cd .. && \
    rm -rf snap-stanford-5.0.0-5.0-ubuntu18.04.2-x64-py3.6 && \
    rm snap-stanford-5.0.0-5.0-ubuntu18.04.2-x64-py3.6.tar.gz

RUN pip3 install --quiet --no-cache-dir gnuplot_kernel graphviz && \
    #jupyter labextension install jupyterlab_graphviz && \
    #jupyter lab build && \
    #jupyter lab clean -y && \
    npm cache clean --force && \
    fix-permissions "${CONDA_DIR}" && \
    fix-permissions "/home/${NB_USER}"

USER $NB_UID

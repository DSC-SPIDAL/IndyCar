import setuptools

with open("../README.md", "r") as fh:
    long_description = fh.read()

print(setuptools.find_packages())

setuptools.setup(
    name='iuindycar',
    version='0.0.8',
    author="Chathura Widanage",
    author_email="chathurawidanage@gmail.com",
    description="This package can be used to communicate with the stream processing kubernetes cluster",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/DSC-SPIDAL/IndyCar",
    packages=setuptools.find_packages(),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
    ],
    python_requires='>=3.6',
    install_requires=[
        'paho-mqtt',
        'kubernetes'
    ],
)

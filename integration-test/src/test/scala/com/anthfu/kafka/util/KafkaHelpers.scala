package com.anthfu.kafka.util

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class ProducerContainer(image: DockerImageName)
  extends GenericContainer[ProducerContainer](image)

class ConsumerContainer(image: DockerImageName)
  extends GenericContainer[ConsumerContainer](image)
